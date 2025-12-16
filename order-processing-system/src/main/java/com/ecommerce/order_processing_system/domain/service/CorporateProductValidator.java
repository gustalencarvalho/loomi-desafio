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
import java.util.Objects;
import java.util.Optional;

import static com.ecommerce.order_processing_system.domain.OrderStatus.CREDIT_LIMIT_EXCEEDED;
import static com.ecommerce.order_processing_system.domain.OrderStatus.INVALID_CORPORATE_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class CorporateProductValidator implements ProductValidator {

    @Value("${app.order.corporate-volume-discount-threshold}")
    private Integer corporateVolumeThreshold;

    @Value("${app.order.high-value-threshold}")
    private String highValueThreshold;

    private final CorporatePolicy corporatePolicy;

    @Override
    public void validate(Order order, OrderItem item) {
        log.debug("Validating CORPORATE productId={} for orderId={} quantity={}", item.getProductId(), order.getOrderId(), item.getQuantity());

        boolean hasInvalidMetadata = order.getItems().stream()
                .anyMatch(i -> i.getMetadata() == null);

        if (hasInvalidMetadata) {
            throw new InvalidCorporateDataException(INVALID_CORPORATE_DATA);
        }

        String cnpj = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .filter(Objects::nonNull)
                .map(metadata -> metadata.get("cnpj"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .orElseThrow(() ->
                        new InvalidCorporateDataException(INVALID_CORPORATE_DATA)
                );

        if (!CnpjValidator.isValidCnpj(cnpj)) {
            throw new InvalidCorporateDataException(INVALID_CORPORATE_DATA);
        }

        if (order.getTotalAmount().compareTo(new BigDecimal(highValueThreshold)) > 0) {
            log.error("CREDIT_LIMIT_EXCEEDED for orderId={}", order.getOrderId());
            throw new CreditLimitExceededException(CREDIT_LIMIT_EXCEEDED);
        }

        int quantity = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() > corporateVolumeThreshold) {
            corporatePolicy.calculateDiscount(order);
        }

        Optional<String> paymentTerms = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .filter(Objects::nonNull)
                .map(metadata -> metadata.get("paymentTerms"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        paymentTerms.ifPresent(payment -> {
            corporatePolicy.calculateDeliveryDate(payment);
        });
    }

}
