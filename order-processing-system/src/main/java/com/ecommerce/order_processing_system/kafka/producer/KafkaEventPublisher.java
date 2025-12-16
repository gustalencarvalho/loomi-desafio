package com.ecommerce.order_processing_system.kafka.producer;

import com.ecommerce.order_processing_system.kafka.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    @Value("${app.order.topic.pending-approval}")
    private String pendingApproval;

    @Value("${app.order.topic.failed}")
    private String topicFailed;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCreated(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for orderId={} to topic={}", event.getPayload().getOrderId(), topicCreated);
        log.debug("OrderCreatedEvent payload={}", event);
        kafkaTemplate.send(topicCreated, event.getPayload().getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishProcessed(OrderProcessedEvent event) {
        log.info("Publishing OrderProcessedEvent for orderId={} to topic={}",  event.getPayload().getOrderId(), topicProcessed);
        log.debug("OrderProcessedEvent payload={}", event);
        kafkaTemplate.send(topicProcessed, event.getPayload().getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishLowStockAlert(LowStockAlertEvent event) {
        log.info("Publishing LowStockAlertEvent for productId={} to topic={}",  event.getPayload().getProductId(), topicLowStock);
        log.debug("LowStockAlertEvent payload={}", event);
        kafkaTemplate.send(topicLowStock, event.getPayload().getProductId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFraudAlert(OrderFraudEvent event) {
        log.info("Publishing OrderFraudEvent for orderId={} to topic={}",  event.getPayload().getOrderId(), topicFraudAlert);
        log.debug("OrderFraudEvent payload={}", event);
        kafkaTemplate.send(topicFraudAlert, event.getPayload().getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishSchedulingPayment(OrderSchedulingPaymentEvent event) {
        log.info("Publishing SchedulingPayment for orderId={} to topic={}",  event.getPayload().getOrderId(), topicSchedulingPayment);
        log.debug("OrderSchedulingPaymentEvent payload={}", event);
        kafkaTemplate.send(topicSchedulingPayment, event.getPayload().getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPendingApproval(OrderPendingApprovalEvent event) {
        log.info("Publishing PendingApproval for orderId={} to topic={}",  event.getPayload().getOrderId(), pendingApproval);
        log.debug("PendingApproval payload={}", event);
        kafkaTemplate.send(pendingApproval, event.getPayload().getOrderId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFailed(OrderFailedEvent event) {
        log.warn("Publishing OrderFailedEvent for orderId={} to topic={}", event.getPayload().getOrderId(), topicFailed);
        log.debug("OrderFailedEvent payload={}", event);
        kafkaTemplate.send(topicFailed, event.getPayload().getOrderId(), event);
    }
}