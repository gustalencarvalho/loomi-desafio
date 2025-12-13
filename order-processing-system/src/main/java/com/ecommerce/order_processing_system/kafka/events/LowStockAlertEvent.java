package com.ecommerce.order_processing_system.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockAlertEvent {
    private String eventId;
    private String eventType;
    private String timestamp;
    private LowStockAlertPayload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockAlertPayload {
        private String orderId;
        private String productId;
        private Integer currentStock;
    }

    public static LowStockAlertEvent create(String orderId, String productId, Integer currentStock) {
        return LowStockAlertEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("LOW_STOCK_ALERT")
                .timestamp(Instant.now().toString())
                .payload(LowStockAlertPayload.builder()
                        .orderId(orderId)
                        .productId(productId)
                        .currentStock(currentStock)
                        .build())
                .build();
    }
}