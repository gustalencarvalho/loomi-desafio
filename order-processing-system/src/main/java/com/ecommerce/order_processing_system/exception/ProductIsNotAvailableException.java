package com.ecommerce.order_processing_system.exception;

public class ProductIsNotAvailableException extends RuntimeException {
    public ProductIsNotAvailableException(String message) {
        super(message);
    }
}
