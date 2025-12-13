package com.ecommerce.order_processing_system.events;

import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {

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
        private String customerId;
        private List<OrderItemResponse> items;
        private BigDecimal totalAmount;
    }

    public static OrderCreatedEvent of(String orderId,
                                       String customerId,
                                       List<OrderItemResponse> items,
                                       BigDecimal total) {
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .timestamp(Instant.now().toString())
                .payload(Payload.builder()
                        .orderId(orderId)
                        .customerId(customerId)
                        .items(items)
                        .totalAmount(total)
                        .build())
                .build();
    }
}