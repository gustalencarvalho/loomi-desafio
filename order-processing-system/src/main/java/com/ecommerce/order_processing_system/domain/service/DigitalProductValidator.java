package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.DigitalPolicy;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.AlreadyOwnedDigitalProductException;
import com.ecommerce.order_processing_system.exception.LicenseUnavailableException;
import com.ecommerce.order_processing_system.service.OrderService;
import com.ecommerce.order_processing_system.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.ecommerce.order_processing_system.domain.OrderStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DigitalProductValidator implements ProductValidator {

    private final OrderService orderService;
    private final ProductService productService;
    private final DigitalPolicy digitalPolicy;

    @Override
    public void validate(Order order, OrderItem item) {
        log.debug("Validating DIGITAL productId={} for orderId={}", item.getProductId(), order.getOrderId());

        ProductDTO product = productService.getProductOrThrow(item.getProductId());

        boolean clientAlreadyOwnsProduct =
                orderService.getOrdersByCustomer(order.getCustomerId()).stream()
                        .filter(o -> o.getStatus() == PROCESSED)
                        .flatMap(o -> o.getItems().stream())
                        .anyMatch(i ->
                                i.getProductId().equals(item.getProductId())
                        );

        if (clientAlreadyOwnsProduct) {
            throw new AlreadyOwnedDigitalProductException(ALREADY_OWNED);
        }

        Object availableLicensesObj = product.getMetadata().get("licensesAvailable");

        if (availableLicensesObj == null) {
            log.error("licensesAvailable not found for productId={}", item.getProductId());
            throw new LicenseUnavailableException(LICENSE_UNAVAILABLE);
        }

        int availableLicenses;
        try {
            availableLicenses = Integer.parseInt(availableLicensesObj.toString());
        } catch (NumberFormatException e) {
            log.error("Invalid licensesAvailable format for productId={}", item.getProductId(), e);
            throw new LicenseUnavailableException(LICENSE_UNAVAILABLE);
        }

        if (availableLicenses <= 0) {
            log.error("No licenses available for productId={}", item.getProductId());
            throw new LicenseUnavailableException(LICENSE_UNAVAILABLE);
        }


        String licenseKey = UUID.randomUUID().toString();
        log.info("Key active created={}", licenseKey);
        digitalPolicy.sendEmail(order.getOrderId(), licenseKey);
    }

}