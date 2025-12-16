package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.PreOrderPolicy;
import com.ecommerce.order_processing_system.exception.PreOrderSoldOutException;
import com.ecommerce.order_processing_system.exception.ReleaseDatePassedException;
import com.ecommerce.order_processing_system.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreOrderProductValidatorTest {

    @Mock
    private ProductService productService;

    @Mock
    private PreOrderPolicy preOrderPolicy;

    private PreOrderProductValidator validator;

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        validator = new PreOrderProductValidator(productService, preOrderPolicy);

        item = OrderItem.builder()
                .productId("PRE-1")
                .quantity(2)
                .metadata(new HashMap<>(Map.of(
                        "releaseDate", LocalDate.now().plusDays(10).toString()
                )))
                .build();

        order = Order.builder()
                .orderId("ORDER-1")
                .items(List.of(item))
                .build();
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateHasPassed() {
        item.getMetadata().put(
                "releaseDate",
                LocalDate.now().minusDays(1).toString()
        );

        assertThrows(
                ReleaseDatePassedException.class,
                () -> validator.validate(order, item)
        );

        verify(productService, never())
                .reservePreOrderSlots(anyString(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenPreOrderIsSoldOut() {
        when(productService.reservePreOrderSlots("PRE-1", 2))
                .thenReturn(false);

        assertThrows(
                PreOrderSoldOutException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldReservePreOrderSlotsSuccessfully() {
        when(productService.reservePreOrderSlots("PRE-1", 2))
                .thenReturn(true);

        validator.validate(order, item);

        verify(productService)
                .reservePreOrderSlots("PRE-1", 2);
    }
}
