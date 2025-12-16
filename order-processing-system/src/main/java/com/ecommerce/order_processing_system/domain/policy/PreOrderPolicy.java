package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.domain.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreOrderPolicy implements PolicyValidator {

    @Override
    public LocalDateTime calculateDeliveryDate(Order order) {
        log.debug("[DELIVERY PRE-ORDER] Calculating delivery date for orderId={}", order.getOrderId());

        int deliveryDays = new Random().nextInt(6) + 5;

        LocalDateTime createdAt = order.getCreatedAt();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        LocalDateTime deliveryDate = createdAt.plusDays(deliveryDays);

        log.debug("Calculated delivery date: {} (in {} days from order creation)",
                deliveryDate, deliveryDays);

        return deliveryDate;
    }

}
