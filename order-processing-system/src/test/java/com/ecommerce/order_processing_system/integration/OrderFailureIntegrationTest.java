package com.ecommerce.order_processing_system.integration;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderFailureIntegrationTest extends BaseIntegrationTest {

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
    void shouldFailOrderWhenProductIsInactive() {
        String productId = "PRE-PS6-001";

        // Given: produto VÁLIDO na criação (createOrder passa)
        when(productCatalogClient.getProduct(productId))
                .thenReturn(validProductForCreation()); // tem estoque, active=true

        // When: criar pedido via API (dispara evento ORDER_CREATED)
        CreateOrderRequest request = invalidRequestWithInactiveProduct();
        var createResponse = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String orderId = createResponse.getBody().getOrderId();

        // Given: agora mock produto INATIVO para o processamento (listener chama process())
        when(productCatalogClient.getProduct(productId))
                .thenReturn(inactiveProduct()); // active=false → falha no process()

        // Then: aguardar listener consumir → process() falha → status FAILED
        await()
                .atMost(20, SECONDS)  // mais tempo pro listener
                .pollInterval(500, MILLISECONDS)
                .untilAsserted(() -> {
                    OrderResponse order = restTemplate.getForObject("/api/orders/" + orderId, OrderResponse.class);
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
                });
    }

    private ProductDTO validProductForCreation() {
        return ProductDTO.builder()
                .productId("PRE-PS6-001")
                .name("PS6 Pre-order")
                .price(new BigDecimal("599.99"))
                .stockQuantity(5)     // ✅ tem estoque na criação
                .active(true)         // ✅ ativo na criação
                .productType(ProductType.PHYSICAL)
                .metadata(Map.of("releaseDate", "2025-11-15"))
                .build();
    }

    private ProductDTO inactiveProduct() {
        return ProductDTO.builder()
                .productId("PRE-PS6-001")
                .name("PS6 Pre-order")
                .price(new BigDecimal("599.99"))
                .stockQuantity(5)
                .active(false)
                .productType(ProductType.PHYSICAL)
                .metadata(Map.of("releaseDate", "2025-11-15"))
                .build();
    }

    private CreateOrderRequest invalidRequestWithInactiveProduct() {
        return CreateOrderRequest.builder()
                .customerId("customer-101")
                .items(List.of(
                        CreateOrderItemRequest.builder()
                                .productId("PRE-PS6-001")
                                .quantity(1)
                                .metadata(Map.of(
                                        "releaseDate", "2025-11-15"
                                ))
                                .build()
                ))
                .build();
    }

}