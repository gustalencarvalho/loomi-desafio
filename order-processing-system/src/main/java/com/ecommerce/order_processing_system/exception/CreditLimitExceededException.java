package com.ecommerce.order_processing_system.exception;

import com.ecommerce.order_processing_system.domain.OrderStatus;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException(OrderStatus message) {
        super(String.valueOf(message));
    }
}
