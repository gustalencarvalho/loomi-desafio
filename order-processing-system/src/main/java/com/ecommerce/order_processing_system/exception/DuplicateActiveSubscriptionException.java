package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class DuplicateActiveSubscriptionException extends RuntimeException {
    public DuplicateActiveSubscriptionException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
