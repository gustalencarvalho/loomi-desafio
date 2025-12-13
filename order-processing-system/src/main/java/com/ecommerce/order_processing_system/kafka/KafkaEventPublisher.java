package com.ecommerce.order_processing_system.kafka;

import com.ecommerce.order_processing_system.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {

    @Value("${app.order.topic.created}")
    private String topicCreated;

    @Value("${app.order.topic.processed}")
    private String topicProcessed;

    @Value("${app.order.topic.low-stock}")
    private String topicLowStock;

    @Value("${app.order.topic.fraud-alert}")
    private String topicFraudAlert;

    @Value("${app.order.topic.scheguling-payment}")
    private String topicSchedulingPayment;

    @Value("${app.order.topic.failed}")
    private String topicFailed;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId={} to topic={}", event.getPayload().getOrderId(), topicCreated);
        log.debug("OrderCreatedEvent payload={}", event);
        kafkaTemplate.send(topicCreated, event.getPayload().getOrderId(), event);
    }

    public void publishProcessed(OrderProcessedEvent event) {
        log.info("Publishing OrderProcessedEvent for orderId={} to topic={}",  event.getPayload().getOrderId(), topicProcessed);
        log.debug("OrderProcessedEvent payload={}", event);
        kafkaTemplate.send(topicProcessed, event.getPayload().getOrderId(), event);
    }

    public void publishLowStockAlert(LowStockAlertEvent event) {
        log.info("Publishing LowStockAlertEvent for productId={} to topic={}",  event.getPayload().getProductId(), topicLowStock);
        log.debug("LowStockAlertEvent payload={}", event);
        kafkaTemplate.send(topicLowStock, event.getPayload().getProductId(), event);
    }

    public void publishFraudAlert(OrderFraudEvent event) {
        log.info("Publishing OrderFraudEvent for orderId={} to topic={}",  event.getPayload().getOrderId(), topicFraudAlert);
        log.debug("OrderFraudEvent payload={}", event);
        kafkaTemplate.send(topicFraudAlert, event.getPayload().getOrderId(), event);
    }

    public void publishSchedulingPayment(OrderSchedulingPaymentEvent event) {
        log.info("Publishing FraudAlert for orderId={} to topic={}",  event.getPayload().getOrderId(), topicSchedulingPayment);
        log.debug("OrderSchedulingPaymentEvent payload={}", event);
        kafkaTemplate.send(topicSchedulingPayment, event.getPayload().getOrderId(), event);
    }

    public void publishFailed(OrderFailedEvent event) {
        log.warn("Publishing OrderFailedEvent for orderId={} to topic={}", event.getPayload().getOrderId(), topicFailed);
        log.debug("OrderFailedEvent payload={}", event);
        kafkaTemplate.send(topicFailed, event.getPayload().getOrderId(), event);
    }
}