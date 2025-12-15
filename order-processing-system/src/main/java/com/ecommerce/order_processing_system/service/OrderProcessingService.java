package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.service.ProductValidator;
import com.ecommerce.order_processing_system.domain.service.ProductValidatorFactory;
import com.ecommerce.order_processing_system.exception.FraudDetectedException;
import com.ecommerce.order_processing_system.exception.OrderNotFoundException;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.OrderFailedEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderProcessedEvent;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

import static com.ecommerce.order_processing_system.domain.OrderStatus.FRAUD_DETECTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {
    private final OrderRepository repository;
    private final ProductValidatorFactory validatorFactory;
    private final KafkaEventPublisher eventPublisher;

    @Value("${app.order.high-value-threshold}")
    private BigDecimal highValueThreshold;

    @Value("${app.order.fraud-check-threshold}")
    private BigDecimal fraudCheckThreshold;

    @Transactional
    public void process(String orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        validateGlobal(order);

        try {

            for (OrderItem item : order.getItems()) {
                ProductValidator validator = validatorFactory.getValidator(item.getProductType());
                validator.validate(order, item);
            }

            order.setStatus(OrderStatus.PROCESSED);
            eventPublisher.publishProcessed(OrderProcessedEvent.of(orderId));
        } catch (RuntimeException e) {
            order.setStatus(OrderStatus.FAILED);
            eventPublisher.publishFailed(OrderFailedEvent.of(orderId, e.getMessage()));
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
                throw new FraudDetectedException(FRAUD_DETECTED);
            }

            log.debug("Fraud check passed for orderId={}", order.getOrderId());
        }
    }
}