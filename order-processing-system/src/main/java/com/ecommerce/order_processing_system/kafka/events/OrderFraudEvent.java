package com.ecommerce.order_processing_system.kafka.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFraudEvent {
    private String eventId;
    private String eventType;
    private String timestamp;
    private OrderFailedEvent.Payload payload;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private String orderId;
        private String reason;
        private String failedAt;
    }

    public static OrderFraudEvent of(String orderId, String reason) {
        String now = Instant.now().toString();
        return OrderFraudEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_FRAUD_CHECK")
                .timestamp(now)
                .payload(OrderFailedEvent.Payload.builder()
                        .orderId(orderId)
                        .reason(reason)
                        .failedAt(now)
                        .build())
                .build();
    }
}
