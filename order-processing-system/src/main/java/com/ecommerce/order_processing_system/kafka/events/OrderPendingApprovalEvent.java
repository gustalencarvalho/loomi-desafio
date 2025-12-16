package com.ecommerce.order_processing_system.kafka.events;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

import static com.ecommerce.order_processing_system.domain.OrderStatus.PENDING_APPROVAL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPendingApprovalEvent {
    private String eventId;
    private String eventType;
    private String timestamp;
    private Payload payload;

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

    public static OrderPendingApprovalEvent of(String orderId, String reason) {
        String now = Instant.now().toString();
        return OrderPendingApprovalEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(PENDING_APPROVAL.name())
                .timestamp(now)
                .payload(Payload.builder()
                        .orderId(orderId)
                        .reason(reason)
                        .failedAt(now)
                        .build())
                .build();
    }
}