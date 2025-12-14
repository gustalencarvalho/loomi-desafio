package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhysicalPolicy {

    public LocalDateTime calculateDeliveryDate(Order order, ProductDTO product) {
        log.debug("Calculating delivery date for productId={}", product.getProductId());

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

    public void paymentCarriedOut(BigDecimal totalAmount) {
        log.info("Payment carried out successfully total={}", totalAmount);
    }
}
