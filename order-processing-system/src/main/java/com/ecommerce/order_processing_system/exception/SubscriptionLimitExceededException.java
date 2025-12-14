package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class SubscriptionLimitExceededException extends RuntimeException {
    public SubscriptionLimitExceededException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
