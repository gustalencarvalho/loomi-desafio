package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.domain.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PolicyValidator {
    LocalDateTime calculateDeliveryDate(Order order);
}
