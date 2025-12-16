package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.exception.InvalidPaymentTermsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static com.ecommerce.order_processing_system.domain.OrderStatus.INVALID_PAYMENT_TERMS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorporatePolicy {

    @Value("${app.order.corporate-discount}")
    private String corporateDiscount;

    public void calculateDeliveryDate(String paymentTerms) {

        LocalDate dueDate = switch (paymentTerms) {
            case "NET_30" -> LocalDate.now().plusDays(30);
            case "NET_60" -> LocalDate.now().plusDays(60);
            case "NET_90" -> LocalDate.now().plusDays(90);
            default -> throw new InvalidPaymentTermsException(INVALID_PAYMENT_TERMS);
        };
        log.info("Due date {}", dueDate);
    }

    public void calculateDiscount(Order order) {

        if (order == null || order.getTotalAmount() == null) {
            log.warn("Corporate discount skipped: null order/total");
            return;
        }

        BigDecimal total = order.getTotalAmount();
        BigDecimal discountRate = new BigDecimal(corporateDiscount);
        BigDecimal discount = total.multiply(discountRate);
        BigDecimal totalWithDiscount = total.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        order.setTotalAmount(totalWithDiscount);

        log.info("orderId={} CORPORATE volume discount: {}% | was={} → now={}",
                order.getOrderId(),
                corporateDiscount,
                total,
                totalWithDiscount);
    }
}
