package com.ecommerce.order_processing_system.kafka.events;

import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSchedulingPaymentEvent {
    private String eventId;
    private String eventType;
    private String timestamp;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private String orderId;
        private String customerId;
        private List<OrderItemResponse> items;
        private BigDecimal totalAmount;
        private String paymenteDate;
    }

    public static OrderSchedulingPaymentEvent of(String orderId,
                                                 String customerId,
                                                 List<OrderItemResponse> items,
                                                 BigDecimal total) {
        return OrderSchedulingPaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_SCHEDULING_PAYMENT")
                .timestamp(Instant.now().toString())
                .payload(Payload.builder()
                        .orderId(orderId)
                        .customerId(customerId)
                        .items(items)
                        .totalAmount(total)
                        .paymenteDate(LocalDate.now().plusDays(30).toString())
                        .build())
                .build();
    }
}
