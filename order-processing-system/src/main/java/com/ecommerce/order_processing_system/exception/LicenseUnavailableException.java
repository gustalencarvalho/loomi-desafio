package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class LicenseUnavailableException extends RuntimeException {
    public LicenseUnavailableException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
