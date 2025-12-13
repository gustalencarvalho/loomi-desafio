package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.events.OrderFailedEvent;
import com.ecommerce.order_processing_system.events.OrderProcessedEvent;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository repository;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.order.high-value-threshold}")
    private BigDecimal highValueThreshold;

    @Value("${app.order.fraud-check-threshold}")
    private BigDecimal fraudCheckThreshold;

    @Value("${app.order.subscription-limit}")
    private Integer subscriptionLimit;

    @Value("${app.order.corporate-approval-threshold}")
    private BigDecimal corporateApprovalThreshold;

    @Transactional
    public void process(String orderId) {

        log.info("Starting processing for orderId={}", orderId);

        Order order = repository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order {} not found during processing", orderId);
                    return new IllegalArgumentException("Order " + orderId + " not found");
                });

        log.debug("Loaded order: {}", order);

        try {
            log.info("Running global validations for orderId={}", orderId);
            validateGlobal(order);

            log.info("Running item-level validations for orderId={}", orderId);
            order.getItems().forEach(item -> {
                log.debug("Validating item: productId={}, type={}, qty={}",
                        item.getProductId(), item.getProductType(), item.getQuantity());

                ProductDTO product = productService.getProductOrThrow(item.getProductId());
                log.debug("Fetched product info for productId={}: {}", item.getProductId(), product);

                switch (item.getProductType()) {
                    case PHYSICAL -> {
                        log.debug("Executing PHYSICAL item validation for productId={}", item.getProductId());
                        validatePhysical(order, product, item.getQuantity());
                    }
                    case SUBSCRIPTION -> {
                        log.debug("Executing SUBSCRIPTION item validation for productId={}", item.getProductId());
                        validateSubscription(order, product);
                    }
                    case DIGITAL -> {
                        log.debug("Executing DIGITAL item validation for productId={}", item.getProductId());
                        validateDigital(order, product);
                    }
                    case PRE_ORDER -> {
                        log.debug("Executing PRE_ORDER item validation for productId={}", item.getProductId());
                        validatePreOrder(order, product);
                    }
                    case CORPORATE -> {
                        log.debug("Executing CORPORATE item validation for productId={}", item.getProductId());
                        validateCorporate(order, product, item.getQuantity());
                    }
                }
            });

            log.info("Evaluating final decision for orderId={}", orderId);

            if (order.getTotalAmount().compareTo(corporateApprovalThreshold) > 0 &&
                    order.getItems().stream().anyMatch(i -> i.getProductType().name().equals("CORPORATE"))) {

                log.info("orderId={} requires corporate approval", orderId);
                order.setStatus(OrderStatus.PENDING_APPROVAL);

            } else {
                log.info("orderId={} processed successfully", orderId);
                order.setStatus(OrderStatus.PROCESSED);
            }

            eventPublisher.publishEvent(OrderProcessedEvent.of(orderId));

            log.info("Finished processing orderId={}", orderId);

        } catch (RuntimeException e) {
            log.error("Order processing FAILED for orderId={}, reason={}", orderId, e.getMessage());

            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason(e.getMessage());

            log.warn("Publishing OrderFailedEvent for orderId={}", orderId);
            eventPublisher.publishEvent(OrderFailedEvent.of(orderId, e.getMessage()));
        }
    }

    private void validateGlobal(Order order) {
        log.debug("Validating global rules for orderId={}, amount={}", order.getOrderId(), order.getTotalAmount());

        if (order.getTotalAmount().compareTo(highValueThreshold) > 0) {
            log.info("High value order detected: orderId={}, amount={}", order.getOrderId(), order.getTotalAmount());
        }

        if (order.getTotalAmount().compareTo(fraudCheckThreshold) > 0) {
            log.info("Running probabilistic fraud check for orderId={}", order.getOrderId());

            if (new Random().nextDouble() < 0.05) {
                log.error("Fraud check FAILED for orderId={}", order.getOrderId());
                throw new RuntimeException("FraudAlert: possible fraud detected");
            }

            log.debug("Fraud check passed for orderId={}", order.getOrderId());
        }
    }

    private void validatePhysical(Order order, ProductDTO product, int qty) {
        log.debug("Validating PHYSICAL item: productId={}, qty={}, stock={}",
                product.getProductId(), qty, product.getStockQuantity());

        if (product.getStockQuantity() == null || product.getStockQuantity() < qty) {
            log.error("OUT_OF_STOCK detected for productId={} in orderId={}", product.getProductId(), order.getOrderId());
            throw new RuntimeException("OUT_OF_STOCK");
        }
    }

    private void validateSubscription(Order order, ProductDTO product) {
        log.debug("Validating SUBSCRIPTION productId={} for orderId={}", product.getProductId(), order.getOrderId());
    }

    private void validateDigital(Order order, ProductDTO product) {
        log.debug("Validating DIGITAL productId={} for orderId={}", product.getProductId(), order.getOrderId());
    }

    private void validatePreOrder(Order order, ProductDTO product) {
        log.debug("Validating PRE_ORDER productId={} for orderId={}",
                product.getProductId(), order.getOrderId());

        Map<String, Object> metadata = product.getMetadata();

        if (metadata == null) {
            log.debug("No metadata for PRE_ORDER validation productId={}", product.getProductId());
            return;
        }

        Object releaseDateRaw = metadata.get("releaseDate");

        if (releaseDateRaw != null) {
            LocalDate release = LocalDate.parse(releaseDateRaw.toString());
            log.debug("PRE_ORDER releaseDate={} for productId={}", release, product.getProductId());

            if (!release.isAfter(LocalDate.now())) {
                log.error("RELEASE_DATE_PASSED for productId={} in orderId={}",
                        product.getProductId(), order.getOrderId());
                throw new RuntimeException("RELEASE_DATE_PASSED");
            }
        }
    }



    private void validateCorporate(Order order, ProductDTO product, int qty) {
        log.debug("Validating CORPORATE productId={} for orderId={} qty={}", product.getProductId(), order.getOrderId(), qty);

        if (order.getTotalAmount().compareTo(new BigDecimal("100000")) > 0) {
            log.error("CREDIT_LIMIT_EXCEEDED for orderId={}", order.getOrderId());
            throw new RuntimeException("CREDIT_LIMIT_EXCEEDED");
        }
    }
}
