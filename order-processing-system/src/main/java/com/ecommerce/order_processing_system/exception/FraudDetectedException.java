package com.ecommerce.order_processing_system.exception;

public class FraudDetectedException extends RuntimeException {
    public FraudDetectedException(String message) {
        super(message);
    }
}
