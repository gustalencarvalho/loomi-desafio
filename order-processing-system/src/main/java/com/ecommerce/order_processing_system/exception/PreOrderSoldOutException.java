package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class PreOrderSoldOutException extends RuntimeException {
    public PreOrderSoldOutException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
