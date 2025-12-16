package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class SlotLimitException extends RuntimeException {
    public SlotLimitException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
