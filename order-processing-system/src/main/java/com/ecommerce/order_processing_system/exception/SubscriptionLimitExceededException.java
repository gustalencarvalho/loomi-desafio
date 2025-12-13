package com.ecommerce.order_processing_system.exception;

public class SubscriptionLimitExceededException extends RuntimeException {
    public SubscriptionLimitExceededException(String message) {
        super(message);
    }
}
