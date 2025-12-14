package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.PhysicalPolicy;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.OutOfStockException;
import com.ecommerce.order_processing_system.kafka.events.LowStockAlertEvent;
import com.ecommerce.order_processing_system.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.ecommerce.order_processing_system.domain.OrderStatus.OUT_OF_STOCK;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhysicalProductValidator implements ProductValidator {

    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final PhysicalPolicy physicalPolicy;

    @Value("${app.order.stock-zero}")
    private int stockZero;

    @Value("${app.order.alert-stock-low}")
    private Integer alertSotckLow;

    @Override
    public void validate(Order order, OrderItem item) {
        log.debug("Validating PHYSICAL item: productId={}, quantity={}, stock={}",
                item.getProductId(), item.getQuantity(), item.getQuantity());
        ProductDTO product = productService.getProductOrThrow(item.getProductId());

        if (item.getQuantity() == stockZero) {
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        if (!product.getActive()) {
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        int newStock = product.getStockQuantity() - item.getQuantity();
        boolean reserved = productService.updateStock(product.getProductId(), newStock);

        if (!reserved) {
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        if (product.getStockQuantity() < alertSotckLow) {
            log.warn("Low stock detected {} ", product.getStockQuantity());
            LowStockAlertEvent lowStockAlertEvent = LowStockAlertEvent.create(
                    order.getOrderId(),
                    product.getProductId(),
                    newStock
            );
            eventPublisher.publishEvent(lowStockAlertEvent);
        }

        log.info("Quantity stock now after reserve {} ", newStock);
        physicalPolicy.paymentCarriedOut(order.getTotalAmount());
        LocalDateTime deliveryDate = physicalPolicy.calculateDeliveryDate(order, product);
    }

}
