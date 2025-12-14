package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.DigitalPolicy;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.AlreadyOwnedDigitalProductException;
import com.ecommerce.order_processing_system.exception.LicenseUnavailableException;
import com.ecommerce.order_processing_system.service.OrderService;
import com.ecommerce.order_processing_system.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ecommerce.order_processing_system.domain.OrderStatus.PROCESSED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalProductValidatorTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ProductService productService;

    @Mock
    private DigitalPolicy digitalPolicy;

    private DigitalProductValidator validator;

    private Order order;
    private OrderItem item;
    private ProductDTO product;

    @BeforeEach
    void setUp() {
        validator = new DigitalProductValidator(orderService, productService, digitalPolicy);

        item = OrderItem.builder()
                .productId("DIGI-1")
                .quantity(1)
                .build();

        order = Order.builder()
                .orderId("ORDER-1")
                .customerId("CUSTOMER-1")
                .items(List.of(item))
                .build();

        product = ProductDTO.builder()
                .productId("DIGI-1")
                .active(true)
                .metadata(new HashMap<>(Map.of(
                        "licensesAvailable", 5
                )))
                .build();
    }

    @Test
    void shouldThrowExceptionWhenCustomerAlreadyOwnsDigitalProduct() {
        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .productId("DIGI-1")
                .build();

        OrderResponse previousOrder = OrderResponse.builder()
                .status(PROCESSED)
                .items(List.of(itemResponse))
                .build();

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of(previousOrder));

        when(productService.getProductOrThrow("DIGI-1"))
                .thenReturn(product);

        assertThrows(
                AlreadyOwnedDigitalProductException.class,
                () -> validator.validate(order, item)
        );

        verify(digitalPolicy, never()).sendEmail(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenLicensesNotPresent() {
        product.getMetadata().remove("licensesAvailable");

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of());

        when(productService.getProductOrThrow("DIGI-1"))
                .thenReturn(product);

        assertThrows(
                LicenseUnavailableException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldThrowExceptionWhenLicensesIsInvalidFormat() {
        product.getMetadata().put("licensesAvailable", "abc");

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of());

        when(productService.getProductOrThrow("DIGI-1"))
                .thenReturn(product);

        assertThrows(
                LicenseUnavailableException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldThrowExceptionWhenNoLicensesAvailable() {
        product.getMetadata().put("licensesAvailable", 0);

        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of());

        when(productService.getProductOrThrow("DIGI-1"))
                .thenReturn(product);

        assertThrows(
                LicenseUnavailableException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldSendEmailWithLicenseKeyWhenValid() {
        when(orderService.getOrdersByCustomer("CUSTOMER-1"))
                .thenReturn(List.of());

        when(productService.getProductOrThrow("DIGI-1"))
                .thenReturn(product);

        validator.validate(order, item);

        verify(digitalPolicy).sendEmail(eq("ORDER-1"), anyString());
    }
}
