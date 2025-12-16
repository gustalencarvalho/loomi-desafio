package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.PhysicalPolicy;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.OutOfStockException;
import com.ecommerce.order_processing_system.kafka.producer.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.LowStockAlertEvent;
import com.ecommerce.order_processing_system.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalProductValidatorTest {

    @Mock
    private ProductService productService;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @Mock
    private PhysicalPolicy physicalPolicy;

    private PhysicalProductValidator validator;

    private Order order;
    private OrderItem item;
    private ProductDTO product;

    @BeforeEach
    void setUp() {
        validator = new PhysicalProductValidator(
                productService,
                eventPublisher,
                physicalPolicy
        );

        // simula @Value
        ReflectionTestUtils.setField(validator, "stockZero", 0);
        ReflectionTestUtils.setField(validator, "alertSotckLow", 5);

        item = OrderItem.builder()
                .productId("PROD-1")
                .quantity(2)
                .build();

        order = Order.builder()
                .orderId("ORDER-1")
                .totalAmount(new BigDecimal("100.00"))
                .build();

        product = ProductDTO.builder()
                .productId("PROD-1")
                .active(true)
                .stockQuantity(10)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        item.setQuantity(0);

        when(productService.getProductOrThrow("PROD-1"))
                .thenReturn(product);

        assertThrows(
                OutOfStockException.class,
                () -> validator.validate(order, item)
        );

        verifyNoInteractions(eventPublisher, physicalPolicy);
    }

    @Test
    void shouldThrowExceptionWhenProductIsInactive() {
        product.setActive(false);

        when(productService.getProductOrThrow("PROD-1"))
                .thenReturn(product);

        assertThrows(
                OutOfStockException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldThrowExceptionWhenStockReservationFails() {
        when(productService.getProductOrThrow("PROD-1"))
                .thenReturn(product);

        when(productService.updateStock("PROD-1", 8))
                .thenReturn(false);

        assertThrows(
                OutOfStockException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldPublishLowStockEventWhenStockBelowThreshold() {
        product.setStockQuantity(4); // abaixo do alertSotckLow

        when(productService.getProductOrThrow("PROD-1"))
                .thenReturn(product);

        when(productService.updateStock("PROD-1", 2))
                .thenReturn(true);

        validator.validate(order, item);

        verify(eventPublisher).publishLowStockAlert(any(LowStockAlertEvent.class));
    }

    @Test
    void shouldProcessSuccessfullyWhenStockIsAvailable() {
        when(productService.getProductOrThrow("PROD-1"))
                .thenReturn(product);

        when(productService.updateStock("PROD-1", 8))
                .thenReturn(true);

        when(physicalPolicy.calculateDeliveryDate(order))
                .thenReturn(LocalDateTime.now().plusDays(5));

        validator.validate(order, item);

        verify(physicalPolicy).paymentCarriedOut(order.getTotalAmount());
        verify(physicalPolicy).calculateDeliveryDate(order);
    }
}
