package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.domain.policy.SubscriptionPolicy;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.DuplicateActiveSubscriptionException;
import com.ecommerce.order_processing_system.exception.SubscriptionLimitExceededException;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.OrderSchedulingPaymentEvent;
import com.ecommerce.order_processing_system.service.OrderService;
import com.ecommerce.order_processing_system.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.ecommerce.order_processing_system.domain.OrderStatus.PROCESSED;
import static com.ecommerce.order_processing_system.domain.ProductType.SUBSCRIPTION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionProductValidatorTest {

    @Mock
    private OrderService orderService;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @Mock
    private ProductService productService;

    @Mock
    private SubscriptionPolicy subscriptionPolicy;

    private SubscriptionProductValidator validator;

    private Order order;
    private OrderItem item;
    private ProductDTO product;

    @BeforeEach
    void setUp() {
        validator = new SubscriptionProductValidator(
                orderService,
                eventPublisher,
                productService,
                subscriptionPolicy
        );

        // simula @Value
        ReflectionTestUtils.setField(validator, "subscriptionLimit", 2);

        item = OrderItem.builder()
                .productId("SUB-1")
                .productType(SUBSCRIPTION)
                .quantity(1)
                .build();

        order = Order.builder()
                .orderId("ORDER-1")
                .customerId("CUSTOMER-1")
                .items(List.of(item))
                .totalAmount(new BigDecimal("100.00"))
                .build();

        product = ProductDTO.builder()
                .productId("SUB-1")
                .build();
    }

    @Test
    void shouldThrowExceptionWhenSubscriptionLimitExceeded() {
        OrderItemResponse previousItem = OrderItemResponse.builder()
                .productType(ProductType.SUBSCRIPTION)
                .build();

        OrderResponse previousOrder = OrderResponse.builder()
                .items(List.of(previousItem, previousItem))
                .build();

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of(previousOrder));

        when(productService.getProductOrThrow("SUB-1"))
                .thenReturn(product);

        assertThrows(
                SubscriptionLimitExceededException.class,
                () -> validator.validate(order, item)
        );

        verify(eventPublisher, never()).publishFailed(any());
    }

    @Test
    void shouldThrowExceptionWhenDuplicateActiveSubscriptionExists() {
        OrderItemResponse previousItem = OrderItemResponse.builder()
                .productId("SUB-1")
                .productType(ProductType.SUBSCRIPTION)
                .build();

        OrderResponse previousOrder = OrderResponse.builder()
                .status(PROCESSED)
                .items(List.of(previousItem))
                .build();

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of(previousOrder));

        when(productService.getProductOrThrow("SUB-1"))
                .thenReturn(product);

        assertThrows(
                DuplicateActiveSubscriptionException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldPropagateExceptionWhenSubscriptionsAreIncompatible() {
        OrderResponse previousOrder = OrderResponse.builder()
                .status(PROCESSED)
                .items(List.of())
                .build();

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of(previousOrder));

        when(productService.getProductOrThrow("SUB-1"))
                .thenReturn(product);

        doThrow(new RuntimeException("INCOMPATIBLE"))
                .when(subscriptionPolicy)
                .validate(anySet(), anySet());

        assertThrows(
                RuntimeException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldPublishSchedulingPaymentEventWhenValid() {
        OrderResponse previousOrder = OrderResponse.builder()
                .status(PROCESSED)
                .items(List.of())
                .build();

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of(previousOrder));

        when(productService.getProductOrThrow("SUB-1"))
                .thenReturn(product);

        validator.validate(order, item);

        verify(subscriptionPolicy)
                .validate(anySet(), anySet());

        verify(eventPublisher)
                .publishSchedulingPayment(any(OrderSchedulingPaymentEvent.class));
    }
}
