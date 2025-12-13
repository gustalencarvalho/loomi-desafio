package com.ecommerce.order_processing_system.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "customerId required")
    private String customerId;

    @NotNull(message = "your list is empty")
    @Valid
    private List<CreateOrderItemRequest> items;
}