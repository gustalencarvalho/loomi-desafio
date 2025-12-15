package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.domain.service.ProductValidator;
import com.ecommerce.order_processing_system.domain.service.ProductValidatorFactory;
import com.ecommerce.order_processing_system.exception.OrderNotFoundException;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.OrderFailedEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderProcessedEvent;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductValidatorFactory validatorFactory;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @Mock
    private ProductValidator productValidator;

    @InjectMocks
    private OrderProcessingService service;

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        item = new OrderItem();
        item.setProductType(ProductType.PHYSICAL);

        order = new Order();
        order.setOrderId("ORDER-1");
        order.setItems(List.of(item));
        order.setTotalAmount(new BigDecimal("100"));

        ReflectionTestUtils.setField(service, "highValueThreshold", new BigDecimal("1000"));
        ReflectionTestUtils.setField(service, "fraudCheckThreshold", new BigDecimal("1000"));
    }

    @Test
    void shouldProcessOrderSuccessfully() {
        when(repository.findById("ORDER-1")).thenReturn(Optional.of(order));
        when(validatorFactory.getValidator(ProductType.PHYSICAL)).thenReturn(productValidator);

        service.process("ORDER-1");

        assertEquals(OrderStatus.PROCESSED, order.getStatus());
        verify(productValidator).validate(order, item);
        verify(eventPublisher).publishProcessed(any(OrderProcessedEvent.class));
        verify(eventPublisher, never()).publishFailed(any(OrderFailedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(repository.findById("ORDER-404")).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> service.process("ORDER-404")
        );

        verifyNoInteractions(validatorFactory, eventPublisher);
    }

    @Test
    void shouldFailOrderWhenValidatorThrowsException() {
        when(repository.findById("ORDER-1")).thenReturn(Optional.of(order));
        when(validatorFactory.getValidator(ProductType.PHYSICAL)).thenReturn(productValidator);

        doThrow(new RuntimeException("validation error"))
                .when(productValidator).validate(order, item);

        service.process("ORDER-1");

        assertEquals(OrderStatus.FAILED, order.getStatus());
        verify(eventPublisher).publishFailed(any(OrderFailedEvent.class));
        verify(eventPublisher, never()).publishProcessed(any(OrderProcessedEvent.class));
    }
}
