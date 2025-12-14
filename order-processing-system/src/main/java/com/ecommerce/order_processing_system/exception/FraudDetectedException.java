package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class FraudDetectedException extends RuntimeException {
    public FraudDetectedException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
