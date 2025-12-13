package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.client.ProductCatalogClient;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.ProductIsNotAvailableException;
import com.ecommerce.order_processing_system.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCatalogClient client;

    public ProductDTO getProductOrThrow(String productId) {
        ProductDTO dto = client.getProduct(productId);
        if (dto == null) {
            throw new ProductNotFoundException("Product " + productId + " not found");
        }
        if (Boolean.FALSE.equals(dto.getActive())) {
            throw new ProductIsNotAvailableException("Product " + productId + " is not available");
        }
        return dto;
    }
}