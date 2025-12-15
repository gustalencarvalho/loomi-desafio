package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.policy.SubscriptionPolicy;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.DuplicateActiveSubscriptionException;
import com.ecommerce.order_processing_system.exception.SubscriptionLimitExceededException;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.OrderSchedulingPaymentEvent;
import com.ecommerce.order_processing_system.service.OrderService;
import com.ecommerce.order_processing_system.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ecommerce.order_processing_system.domain.OrderStatus.*;
import static com.ecommerce.order_processing_system.domain.ProductType.SUBSCRIPTION;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionProductValidator implements ProductValidator {
    private final OrderService orderService;
    private final KafkaEventPublisher eventPublisher;

    @Value("${app.order.subscription-limit}")
    private int subscriptionLimit;

    private final ProductService productService;
    private final SubscriptionPolicy isIncompatible;

    @Override
    public void validate(Order order, OrderItem item) {
        List<OrderResponse> orders = orderService.getOrdersByCustomer(order.getCustomerId());
        ProductDTO product = productService.getProductOrThrow(item.getProductId());

        long subscriptionCount = orders.stream()
                .flatMap(orderItem -> orderItem.getItems().stream())
                .filter(it -> SUBSCRIPTION.equals(it.getProductType()))
                .count();

        if (subscriptionCount >= subscriptionLimit) {
            log.warn("Subscription limit exceeded. customerId={}, total={}, limit={}",
                    order.getCustomerId(),
                    subscriptionCount,
                    subscriptionLimit
            );

            throw new SubscriptionLimitExceededException(SUBSCRIPTION_LIMIT_EXCEEDED);
        }

        boolean hasDuplicateActiveSubscription = orders.stream()
                .filter(orderFilter -> orderFilter.getStatus() == PROCESSED)
                .flatMap(orderFlat -> orderFlat.getItems().stream())
                .anyMatch(it ->
                        SUBSCRIPTION.equals(it.getProductType()) &&
                                it.getProductId().equals(product.getProductId())
                );

        if (hasDuplicateActiveSubscription) {
            log.info("Duplicate active subscription for product {} ", hasDuplicateActiveSubscription);
            throw new DuplicateActiveSubscriptionException(DUPLICATE_ACTIVE_SUBSCRIPTION);
        }

        Set<String> currentSubscriptions = order.getItems().stream()
                .filter(it -> it.getProductType() == SUBSCRIPTION)
                .map(it -> it.getProductId())
                .collect(Collectors.toSet());

        Set<String> previousSubscriptions = orderService.getOrdersByCustomer(order.getCustomerId()).stream()
                .filter(orderResp -> orderResp.getStatus() == OrderStatus.PROCESSED)
                .flatMap(orderResp -> orderResp.getItems().stream())
                .filter(it -> it.getProductType() == SUBSCRIPTION)
                .map(it -> it.getProductId())
                .collect(Collectors.toSet());

        isIncompatible.validate(currentSubscriptions, previousSubscriptions);

        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList());

        log.debug("Validating SUBSCRIPTION successfully productId={} for orderId={}", product.getProductId(), order.getOrderId());
        eventPublisher.publishSchedulingPayment(OrderSchedulingPaymentEvent.of(order.getOrderId(), order.getCustomerId(), items, order.getTotalAmount()));
    }

    public OrderItemResponse toResponseItem(OrderItem orderItem) {
        log.trace("Mapping OrderItem itemId={} to response", orderItem.getItemId());

        return OrderItemResponse.builder()
                .itemId(orderItem.getItemId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productType(orderItem.getProductType())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getSubtotal())
                .metadata(orderItem.getMetadata())
                .build();
    }

}