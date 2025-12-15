package com.ecommerce.order_processing_system.integration;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.kafka.events.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest {

    @Container
    static RedpandaContainer redpanda =
            new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.3.10");

    @Container
    static PostgreSQLContainer<?> orderPostgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_processing_db")
            .withUsername("postgres")
            .withPassword("15421542");

    @DynamicPropertySource
    static void configureProps(DynamicPropertyRegistry registry) {
        // Postgres (se estiver usando container)
        registry.add("spring.datasource.url", orderPostgres::getJdbcUrl);
        registry.add("spring.datasource.username", orderPostgres::getUsername);
        registry.add("spring.datasource.password", orderPostgres::getPassword);

        // Kafka: sobrescreve exatamente spring.kafka.bootstrap-servers
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);

        // Tópicos (batendo com o @KafkaListener e publisher)
        registry.add("app.order.topic.created", () -> "order-events-created");
        registry.add("app.order.topic.processed", () -> "order-events-processed");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ProductCatalogClient productCatalogClient;

    @Test
    void shouldCreateOrderAndProcessToProcessed() {
        // Mock: produto físico válido
        when(productCatalogClient.getProduct(any())).thenReturn(
                ProductDTO.builder()
                        .productId("PROD-1")
                        .name("Notebook")
                        .price(new BigDecimal("100.00"))
                        .stockQuantity(10)
                        .active(true)
                        .productType(ProductType.PHYSICAL)
                        .metadata(Map.of())
                        .build()
        );
        when(productCatalogClient.updateStock(any(), any())).thenReturn(true);

        // Given: requisição de criação de pedido
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("CUSTOMER-1")
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .productId("PROD-1")
                                .quantity(1)
                                .metadata(Map.of())
                                .build()
                ))
                .build();

        // When: cria pedido via API (controller -> service -> publishEventAfterCommit)
        OrderResponse created =
                restTemplate.postForObject("/api/orders", request, OrderResponse.class);

        String orderId = created.getOrderId();

        // Then: aguarda listener consumir evento CREATED, processar e atualizar para PROCESSED
        await()
                .atMost(15, SECONDS)
                .pollInterval(500, MILLISECONDS)
                .untilAsserted(() -> {
                    OrderResponse order =
                            restTemplate.getForObject("/api/orders/" + orderId, OrderResponse.class);

                    assertEquals(OrderStatus.PROCESSED, order.getStatus());
                    assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
                });
    }
}
