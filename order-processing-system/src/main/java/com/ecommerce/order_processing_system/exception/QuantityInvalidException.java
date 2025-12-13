package com.ecommerce.order_processing_system.exception;

public class QuantityInvalidException extends RuntimeException {
    public QuantityInvalidException(String message) {
        super(message);
    }
}
