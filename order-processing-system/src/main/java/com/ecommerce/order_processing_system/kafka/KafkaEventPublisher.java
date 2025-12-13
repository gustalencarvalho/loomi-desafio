package com.ecommerce.order_processing_system.kafka;

import com.ecommerce.order_processing_system.events.OrderCreatedEvent;
import com.ecommerce.order_processing_system.events.OrderFailedEvent;
import com.ecommerce.order_processing_system.events.OrderProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventPublisher {

    @Value("${app.order.topic.created}")
    private String topicCreated;

    @Value("${app.order.topic.processed}")
    private String topicProcessed;

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

    public void publishFailed(OrderFailedEvent event) {
        log.warn("Publishing OrderFailedEvent for orderId={} to topic={}", event.getPayload().getOrderId(), topicFailed);
        log.debug("OrderFailedEvent payload={}", event);
        kafkaTemplate.send(topicFailed, event.getPayload().getOrderId(), event);
    }
}