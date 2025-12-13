package com.ecommerce.order_processing_system.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFailedEvent {
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
        private String reason;
        private String failedAt;
    }

    public static OrderFailedEvent of(String orderId, String reason) {
        String now = Instant.now().toString();
        return OrderFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_FAILED")
                .timestamp(now)
                .payload(Payload.builder()
                        .orderId(orderId)
                        .reason(reason)
                        .failedAt(now)
                        .build())
                .build();
    }
}