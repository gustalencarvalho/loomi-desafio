package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.ProductIsNotAvailableException;
import com.ecommerce.order_processing_system.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductCatalogClient client;

    @InjectMocks
    private ProductService productService;

    private ProductDTO activeProduct;

    @BeforeEach
    void setUp() {
        activeProduct = new ProductDTO();
        activeProduct.setProductId("PROD-1");
        activeProduct.setActive(true);
    }

    @Test
    void shouldReturnProductWhenProductExistsAndIsActive() {
        when(client.getProduct("PROD-1")).thenReturn(activeProduct);

        ProductDTO result = productService.getProductOrThrow("PROD-1");

        assertNotNull(result);
        assertEquals("PROD-1", result.getProductId());
        verify(client).getProduct("PROD-1");
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(client.getProduct("PROD-404")).thenReturn(null);

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductOrThrow("PROD-404")
        );

        verify(client).getProduct("PROD-404");
    }

    @Test
    void shouldThrowExceptionWhenProductIsInactive() {
        ProductDTO inactiveProduct = new ProductDTO();
        inactiveProduct.setProductId("PROD-2");
        inactiveProduct.setActive(false);

        when(client.getProduct("PROD-2")).thenReturn(inactiveProduct);

        assertThrows(
                ProductIsNotAvailableException.class,
                () -> productService.getProductOrThrow("PROD-2")
        );

        verify(client).getProduct("PROD-2");
    }

    @Test
    void shouldUpdateStockSuccessfully() {
        when(client.updateStock("PROD-1", 10)).thenReturn(true);

        boolean result = productService.updateStock("PROD-1", 10);

        assertTrue(result);
        verify(client).updateStock("PROD-1", 10);
    }

    @Test
    void shouldReservePreOrderSlotsSuccessfully() {
        when(client.reservePreOrderSlots("PROD-1", 5)).thenReturn(true);

        boolean result = productService.reservePreOrderSlots("PROD-1", 5);

        assertTrue(result);
        verify(client).reservePreOrderSlots("PROD-1", 5);
    }
}
