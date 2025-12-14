package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class InvalidPaymentTermsException extends RuntimeException {
    public InvalidPaymentTermsException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
