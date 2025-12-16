package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.PreOrderPolicy;
import com.ecommerce.order_processing_system.exception.PreOrderSoldOutException;
import com.ecommerce.order_processing_system.exception.ReleaseDatePassedException;
import com.ecommerce.order_processing_system.exception.SlotLimitException;
import com.ecommerce.order_processing_system.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static com.ecommerce.order_processing_system.domain.OrderStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreOrderProductValidator implements ProductValidator {

    private final ProductService productService;
    private final PreOrderPolicy preOrderPolicy;

    @Value("${app.order.slot-limit}")
    private Integer limitSlot;

    @Override
    public void validate(Order order, OrderItem item) {
        log.debug("Validating PRE_ORDER productId={} for orderId={}",
                item.getProductId(), order.getOrderId());

        Optional<String> releaseDateRaw = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("releaseDate"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        releaseDateRaw.ifPresent(date -> {
            if (date != null) {
                LocalDate release = LocalDate.parse(date);
                log.debug("PRE_ORDER releaseDate={} for productId={}", release, item.getProductId());

                if (!release.isAfter(LocalDate.now())) {
                    log.error("RELEASE_DATE_PASSED for productId={} in orderId={}",
                            item.getProductId(), order.getOrderId());
                    throw new ReleaseDatePassedException(RELEASE_DATE_PASSED);
                }
            }
        });

        order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(m -> m.get("preOrderSlots"))
                .filter(Objects::nonNull)
                .findFirst()
                .map(Object::toString)
                .map(Integer::parseInt)
                .filter(slot -> slot < item.getQuantity())
                .ifPresent(slot -> {
                    log.warn("Slot limit exceeded: {}", slot);
                    throw new SlotLimitException(PRE_ORDER_SOLD_OUT);
                });

        int requestedQuantity = order.getItems().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .mapToInt(OrderItem::getQuantity)
                .sum();

        boolean reserved = productService.reservePreOrderSlots(item.getProductId(), requestedQuantity);

        if (!reserved) {
            throw new PreOrderSoldOutException(PRE_ORDER_SOLD_OUT);
        }

        preOrderPolicy.calculateDeliveryDate(order);
    }
}
