package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalPolicyTest {

    private PhysicalPolicy physicalPolicy;

    private Order order;
    private ProductDTO product;

    @BeforeEach
    void setUp() {
        physicalPolicy = new PhysicalPolicy();

        order = Order.builder()
                .orderId("ORDER-1")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        product = ProductDTO.builder()
                .productId("PROD-1")
                .build();
    }

    // 🔹 BRANCH 1: createdAt != null
    @Test
    void shouldCalculateDeliveryDateWhenCreatedAtIsPresent() {
        LocalDateTime deliveryDate =
                physicalPolicy.calculateDeliveryDate(order);

        assertNotNull(deliveryDate);
        assertTrue(deliveryDate.isAfter(order.getCreatedAt()));

        long daysBetween =
                java.time.Duration.between(order.getCreatedAt(), deliveryDate).toDays();

        assertTrue(daysBetween >= 5 && daysBetween <= 10);
    }

    // 🔹 BRANCH 2: createdAt == null
    @Test
    void shouldCalculateDeliveryDateWhenCreatedAtIsNull() {
        order.setCreatedAt(null);

        LocalDateTime before = LocalDateTime.now();

        LocalDateTime deliveryDate =
                physicalPolicy.calculateDeliveryDate(order);

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(deliveryDate);
        assertTrue(deliveryDate.isAfter(before));
        assertTrue(deliveryDate.isBefore(after.plusDays(11)));
    }

    @Test
    void shouldLogWhenPaymentIsCarriedOut() {
        assertDoesNotThrow(() ->
                physicalPolicy.paymentCarriedOut(new BigDecimal("199.90"))
        );
    }
}
