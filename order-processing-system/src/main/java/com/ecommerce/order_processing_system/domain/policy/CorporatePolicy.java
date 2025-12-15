package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.exception.InvalidPaymentTermsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.ecommerce.order_processing_system.domain.OrderStatus.INVALID_PAYMENT_TERMS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorporatePolicy {

    public void calculateTermsPayment(String paymentTerms) {
        LocalDate dueDate = switch (paymentTerms) {
            case "NET_30" -> LocalDate.now().plusDays(30);
            case "NET_60" -> LocalDate.now().plusDays(60);
            case "NET_90" -> LocalDate.now().plusDays(90);
            default -> throw new InvalidPaymentTermsException(INVALID_PAYMENT_TERMS);
        };
        log.info("Due date {}", dueDate);
    }
}
