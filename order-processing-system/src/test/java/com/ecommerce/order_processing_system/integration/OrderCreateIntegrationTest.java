package com.ecommerce.order_processing_system.integration;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderCreateIntegrationTest extends BaseIntegrationTest {

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
    @Order(2)
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
