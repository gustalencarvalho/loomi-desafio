package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductValidatorFactoryTest {

    @Mock
    private ProductValidator physicalValidator;

    @Mock
    private ProductValidator digitalValidator;

    private ProductValidatorFactory factory;

    @BeforeEach
    void setUp() {
        Map<ProductType, ProductValidator> validators = new EnumMap<>(ProductType.class);
        validators.put(ProductType.PHYSICAL, physicalValidator);
        validators.put(ProductType.DIGITAL, digitalValidator);

        factory = new ProductValidatorFactory(validators);
    }

    @Test
    void shouldReturnPhysicalValidator() {
        ProductValidator validator = factory.getValidator(ProductType.PHYSICAL);

        assertNotNull(validator);
        assertEquals(physicalValidator, validator);
    }

    @Test
    void shouldReturnDigitalValidator() {
        ProductValidator validator = factory.getValidator(ProductType.DIGITAL);

        assertNotNull(validator);
        assertEquals(digitalValidator, validator);
    }

    @Test
    void shouldThrowExceptionWhenValidatorNotFound() {
        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> factory.getValidator(ProductType.SUBSCRIPTION)
        );

        assertEquals(
                "Validator not found for type SUBSCRIPTION",
                exception.getMessage()
        );
    }
}
