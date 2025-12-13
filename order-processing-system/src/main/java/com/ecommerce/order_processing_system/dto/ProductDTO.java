package com.ecommerce.order_processing_system.dto;

import com.ecommerce.order_processing_system.domain.ProductType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private String productId;
    private String name;
    private ProductType productType;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean active;
    private Map<String, Object> metadata;
}