package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class ReleaseDatePassedException extends RuntimeException {
    public ReleaseDatePassedException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
