package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
