package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductValidatorFactory {
    private final Map<ProductType, ProductValidator> validators;

    public ProductValidator getValidator(ProductType type) {
        ProductValidator validator = validators.get(type);
        if (validator == null) {
            throw new UnsupportedOperationException("Validator not found for type " + type);
        }
        return validator;
    }
}