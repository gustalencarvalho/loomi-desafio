package com.loomi.product_catalog_service.api.dto;

import com.loomi.product_catalog_service.domain.ProductType;

import java.math.BigDecimal;
import java.util.Map;

public record ProductDto(String productId, String name, ProductType productType, BigDecimal price, Integer stockQuantity, Boolean active, Map<String, Object> metadata) {

}
