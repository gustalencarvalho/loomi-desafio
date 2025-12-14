package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class InvalidCorporateDataException extends RuntimeException {
    public InvalidCorporateDataException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
