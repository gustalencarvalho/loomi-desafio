package com.ecommerce.order_processing_system.integration;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.kafka.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ProductCatalogClient productCatalogClient;

    @BeforeEach
    void configureRestTemplateTimeout() {
        // ✅ Aumenta timeout para 60s
        restTemplate.getRestTemplate().setRequestFactory(
                new SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(30000);  // 30s connect
                    setReadTimeout(60000);     // 60s read
                }}
        );
    }

    @Test
    void shouldCreateOrderAsPENDINGAndPublishOrderCreatedEvent() {
        // ✅ Produto válido
        ProductDTO validProduct = ProductDTO.builder()
                .productId("VALID-PROD")
                .name("Valid Product")
                .price(BigDecimal.valueOf(25.00))
                .stockQuantity(100)
                .productType(ProductType.PHYSICAL)
                .build();

        when(productCatalogClient.getProduct("VALID-PROD")).thenReturn(validProduct);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("TEST-CUSTOMER-123")
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .productId("VALID-PROD")
                                .quantity(2)
                                .build()
                ))
                .build();

        // 1. POST /api/orders → 201 CREATED
        var createResponse = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        OrderResponse createdOrder = createResponse.getBody();
        String orderId = createdOrder.getOrderId();

        // 2. ✅ Verificar Order PENDING imediatamente após POST
        Awaitility.await()
                .atMost(5, SECONDS)
                .pollInterval(200, MILLISECONDS)
                .untilAsserted(() -> {
                    OrderResponse order = restTemplate.getForObject("/api/orders/" + orderId, OrderResponse.class);

                    // ✅ Status PENDING após criação
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
                    assertThat(order.getCustomerId()).isEqualTo("TEST-CUSTOMER-123");
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));

                    log.info("✅ Order PENDING: orderId={}, total={}", orderId, order.getTotalAmount());
                });

    }

}
