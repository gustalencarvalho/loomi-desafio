package com.ecommerce.order_processing_system.exception;

public class DuplicateActiveSubscriptionException extends RuntimeException {
    public DuplicateActiveSubscriptionException(String message) {
        super(message);
    }
}
