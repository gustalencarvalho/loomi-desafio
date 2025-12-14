package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.exception.ErrorSystemDefaultException;
import com.ecommerce.order_processing_system.exception.OrderNotFoundException;
import com.ecommerce.order_processing_system.exception.OutOfStockException;
import com.ecommerce.order_processing_system.kafka.events.OrderCreatedEvent;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static com.ecommerce.order_processing_system.domain.OrderStatus.OUT_OF_STOCK;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Starting order creation for customerId={}, items={}",
                request.getCustomerId(), request.getItems().size());

        List<OrderItem> items = request.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.debug("Calculated total order amount={}", total);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .items(items)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .build();

        Order finalOrder = order;
        items.forEach(i -> i.setOrder(finalOrder));
        log.debug("Linked all items to the order");

        order = orderRepository.save(order);
        log.info("Order persisted successfully orderId={}", order.getOrderId());

        OrderResponse response = toResponse(order);
        log.debug("Converted saved order to response object");

        OrderCreatedEvent event = OrderCreatedEvent.of(
                order.getOrderId(),
                order.getCustomerId(),
                response.getItems(),
                response.getTotalAmount()
        );

        eventPublisher.publishEvent(event);

        var responseOrder = OrderResponse.builder()
                .orderId(response.getOrderId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();

        log.info("Order creation completed successfully orderId={}", responseOrder.getOrderId());
        return responseOrder;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        log.info("Fetching order by id={}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found id={}", orderId);
                    return new OrderNotFoundException("Order " + orderId + " not found");
                });

        log.debug("Order found id={} status={}", orderId, order.getStatus());
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        log.info("Fetching orders for customerId={}", customerId);

        List<OrderResponse> list = orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        log.info("Found {} orders for customerId={}", list.size(), customerId);
        return list;
    }

    public OrderItem toOrderItem(CreateOrderItemRequest orderItemRequest) {
        log.debug("Converting request item productId={} qty={}",
                orderItemRequest.getProductId(),
                orderItemRequest.getQuantity());

        var product = productService.getProductOrThrow(orderItemRequest.getProductId());
        log.debug("Found product productId={} price={} stock={}",
                product.getProductId(), product.getPrice(), product.getStockQuantity());

        if (product.getStockQuantity() == null || orderItemRequest.getQuantity() > product.getStockQuantity()) {
            log.error("OUT_OF_STOCK detected for productId={} in orderId={}", product.getProductId(),
                    orderItemRequest.getProductId());
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));
        log.debug("Calculated subtotal={} for productId={}",
                subtotal, product.getProductId());

        String metadataJson = null;

        OrderItem item = OrderItem.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .productType(product.getProductType())
                .quantity(orderItemRequest.getQuantity())
                .price(product.getPrice())
                .subtotal(subtotal)
                .metadata(product.getMetadata())
                .build();

        log.debug("Created OrderItem for productId={} qty={}",
                item.getProductId(), item.getQuantity());

        return item;
    }

    public OrderResponse toResponse(Order order) {
        log.debug("Mapping Order entity to response orderId={}", order.getOrderId());

        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .failureReason(order.getFailureReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderItemResponse toResponseItem(OrderItem orderItem) {
        log.trace("Mapping OrderItem itemId={} to response", orderItem.getItemId());
        String metadataJson = null;

        if (orderItem.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(orderItem.getMetadata());
            } catch (Exception e) {
                throw new ErrorSystemDefaultException(e.getMessage());
            }
        }
        return OrderItemResponse.builder()
                .itemId(orderItem.getItemId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .productType(orderItem.getProductType())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .subtotal(orderItem.getSubtotal())
                .metadata(metadataJson)
                .build();
    }
}
