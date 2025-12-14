package com.ecommerce.order_processing_system.dto;

import com.ecommerce.order_processing_system.domain.ProductType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemResponse {
    private String itemId;
    private String productId;
    private String productName;
    private ProductType productType;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    private Map<String, Object> metadata;
}