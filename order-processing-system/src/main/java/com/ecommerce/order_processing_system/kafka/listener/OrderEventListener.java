package com.ecommerce.order_processing_system.kafka.listener;

import com.ecommerce.order_processing_system.kafka.events.OrderCreatedEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderProcessedEvent;
import com.ecommerce.order_processing_system.service.OrderProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderProcessingService processingService;
    private static final String ORDER_CREATED = "CREATED";
    private static final String ORDER_PROCESSED = "ORDER_PROCESSED";

    @KafkaListener(
            topics = "order-events-created",
            groupId = "order-processing-group"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received message from topic=order-events-created: eventType={}, orderId={}",
                event.getEventType(),
                event.getPayload() != null ? event.getPayload().getOrderId() : "null");

        if (!ORDER_CREATED.equals(event.getEventType())) {
            log.debug("Ignoring event type {} for orderId={}", event.getEventType(),
                    event.getPayload() != null ? event.getPayload().getOrderId() : "null");
            return;
        }

        var orderId = event.getPayload().getOrderId();
        log.info("Processing ORDER_CREATED event for orderId={}", orderId);

        processingService.process(orderId);

        log.info("Finished processing ORDER_CREATED event for orderId={}", orderId);
    }

    @KafkaListener(
            topics = "order-events-processed",
            groupId = "order-processing-group"
    )
    public void onOrderProcessed(OrderProcessedEvent event) {
        log.info("Received message from topic=order-events-processed: eventType={}, orderId={}",
                event.getEventType(),
                event.getPayload() != null ? event.getPayload().getOrderId() : "null");

        if (!ORDER_PROCESSED.equals(event.getEventType())) {
            log.debug("Ignoring event type {} for orderId={}", event.getEventType(),
                    event.getPayload() != null ? event.getPayload().getOrderId() : "null");
            return;
        }

        var orderId = event.getPayload().getOrderId();
        log.info("Consumed ORDER_PROCESSED event for orderId={}.", orderId);
    }
}