package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class WarehouseUnavailableException extends RuntimeException {
    public WarehouseUnavailableException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
