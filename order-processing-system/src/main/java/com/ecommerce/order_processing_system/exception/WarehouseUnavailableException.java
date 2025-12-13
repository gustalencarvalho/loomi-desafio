package com.ecommerce.order_processing_system.exception;

public class WarehouseUnavailableException extends RuntimeException {
    public WarehouseUnavailableException(String message) {
        super(message);
    }
}
