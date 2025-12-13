package com.ecommerce.order_processing_system.kafka.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProcessedEvent {
    private String eventId;
    private String eventType;
    private String timestamp;
    private Payload payload;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private String orderId;
        private String processedAt;
    }

    public static OrderProcessedEvent of(String orderId) {
        String now = Instant.now().toString();
        return OrderProcessedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_PROCESSED")
                .timestamp(now)
                .payload(Payload.builder()
                        .orderId(orderId)
                        .processedAt(now)
                        .build())
                .build();
    }
}