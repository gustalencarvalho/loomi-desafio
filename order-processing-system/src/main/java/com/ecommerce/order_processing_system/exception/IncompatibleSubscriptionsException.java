package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class IncompatibleSubscriptionsException extends RuntimeException {
    public IncompatibleSubscriptionsException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
