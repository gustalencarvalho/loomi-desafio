package com.ecommerce.order_processing_system.exception;

public class AlreadyOwnedDigitalProductException extends RuntimeException {
    public AlreadyOwnedDigitalProductException(String message) {
        super(message);
    }
}
