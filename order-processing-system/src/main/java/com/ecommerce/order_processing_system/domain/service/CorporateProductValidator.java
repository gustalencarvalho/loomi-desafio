package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.CorporatePolicy;
import com.ecommerce.order_processing_system.exception.CreditLimitExceededException;
import com.ecommerce.order_processing_system.exception.InvalidCorporateDataException;
import com.ecommerce.order_processing_system.util.CnpjValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import static com.ecommerce.order_processing_system.domain.OrderStatus.CREDIT_LIMIT_EXCEEDED;
import static com.ecommerce.order_processing_system.domain.OrderStatus.INVALID_CORPORATE_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorporateProductValidator implements ProductValidator {

    @Value("${app.order.corporate-volume-discount-threshold}")
    private Integer orporateVolumeThreshold;

    @Value("${app.order.corporate-discount}")
    private String corporateDiscount;

    private final CorporatePolicy termsPayment;

    @Override
    public void validate(Order order, OrderItem item) {
        log.debug("Validating CORPORATE productId={} for orderId={} quantity={}", item.getProductId(), order.getOrderId(), item.getQuantity());

        Optional<String> firstCnpj = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("cnpj"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        firstCnpj.ifPresent(cnpj -> {
            if (!CnpjValidator.isValidCnpj(cnpj)) {
                log.info("CNPJ invalid: {}", cnpj);
                throw new InvalidCorporateDataException(INVALID_CORPORATE_DATA);
            }
        });

        if (order.getTotalAmount().compareTo(new BigDecimal("100000")) > 0) {
            log.error("CREDIT_LIMIT_EXCEEDED for orderId={}", order.getOrderId());
            throw new CreditLimitExceededException(CREDIT_LIMIT_EXCEEDED);
        }

        int quantity = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (quantity > orporateVolumeThreshold) {
            BigDecimal total = order.getTotalAmount();
            BigDecimal discountRate = new BigDecimal(corporateDiscount);
            BigDecimal discount = total.multiply(discountRate);
            BigDecimal totalWithDiscount = total.subtract(discount).setScale(2, RoundingMode.HALF_UP);
            order.setTotalAmount(totalWithDiscount);
            log.info("Total without discount {} ", total);
            log.info("orderId={} volume discount applied: {}", order.getOrderId(), totalWithDiscount);
        }

        Optional<String> paymentTerms = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("paymentTerms"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        paymentTerms.ifPresent(payment -> {
            termsPayment.calculateTermsPayment(payment);
        });
    }

}
