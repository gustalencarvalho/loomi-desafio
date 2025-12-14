package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.*;
import com.ecommerce.order_processing_system.kafka.events.LowStockAlertEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderFailedEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderProcessedEvent;
import com.ecommerce.order_processing_system.kafka.events.OrderSchedulingPaymentEvent;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import com.ecommerce.order_processing_system.util.CnpjValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ecommerce.order_processing_system.domain.OrderStatus.*;
import static com.ecommerce.order_processing_system.domain.ProductType.SUBSCRIPTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final OrderRepository repository;
    private final OrderService orderService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${app.order.high-value-threshold}")
    private BigDecimal highValueThreshold;

    @Value("${app.order.fraud-check-threshold}")
    private BigDecimal fraudCheckThreshold;

    @Value("${app.order.stock-zero}")
    private Integer stockZero;

    @Value("${app.order.subscription-limit}")
    private Integer subscriptionLimit;

    @Value("${app.order.corporate-discount}")
    private String corporateDiscount;

    @Value("${app.order.corporate-volume-discount-threshold}")
    private Integer orporateVolumeThreshold;

    @Value("${app.order.alert-stock-low}")
    private Integer alertSotckLow;

    @Value("${app.order.corporate-approval-threshold}")
    private BigDecimal corporateApprovalThreshold;

    @Transactional
    public void process(String orderId) {

        log.info("Starting processing for orderId={}", orderId);

        Order order = repository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order {} not found during processing", orderId);
                    return new OrderNotFoundException("Order " + orderId + " not found");
                });

        try {
            log.info("Running global validations for orderId={}", orderId);
            validateGlobal(order);

            log.info("Running item-level validations for orderId={}", orderId);
            order.getItems().forEach(item -> {
                log.debug("Validating item: productId={}, type={}, qty={}",
                        item.getProductId(), item.getProductType(), item.getQuantity());

                ProductDTO product = productService.getProductOrThrow(item.getProductId());
                log.debug("Fetched product info for productId={}: {}", item.getProductId(), product);

                switch (item.getProductType()) {
                    case PHYSICAL -> {
                        log.debug("Executing PHYSICAL item validation for productId={}", item.getProductId());
                        validatePhysical(order, product, item.getQuantity());
                    }
                    case SUBSCRIPTION -> {
                        log.debug("Executing SUBSCRIPTION item validation for productId={}", item.getProductId());
                        validateSubscription(order, product);
                    }
                    case DIGITAL -> {
                        log.debug("Executing DIGITAL item validation for productId={}", item.getProductId());
                        validateDigital(order, product);
                    }
                    case PRE_ORDER -> {
                        log.debug("Executing PRE_ORDER item validation for productId={}", item.getProductId());
                        validatePreOrder(order, product);
                    }
                    case CORPORATE -> {
                        log.debug("Executing CORPORATE item validation for productId={}", item.getProductId());
                        validateCorporate(order, product, item.getQuantity());
                    }
                }
            });

            log.info("Evaluating final decision for orderId={}", orderId);

            if (order.getTotalAmount().compareTo(corporateApprovalThreshold) > 0 &&
                    order.getItems().stream().anyMatch(i -> i.getProductType().name().equals("CORPORATE"))) {

                log.info("orderId={} requires corporate approval", orderId);
                order.setStatus(PENDING_APPROVAL);

            } else {
                log.info("orderId={} processed successfully", orderId);
                order.setStatus(PROCESSED);
            }

            eventPublisher.publishEvent(OrderProcessedEvent.of(orderId));

            log.info("Finished processing orderId={}", orderId);

        } catch (RuntimeException e) {
            log.error("Order processing FAILED for orderId={}, reason={}", orderId, e.getMessage());
            eventPublisher.publishEvent(OrderFailedEvent.of(orderId, e.getMessage()));
        }
    }

    private void validateGlobal(Order order) {
        log.debug("Validating global rules for orderId={}, amount={}", order.getOrderId(), order.getTotalAmount());

        if (order.getTotalAmount().compareTo(highValueThreshold) > 0) {
            log.info("High value order detected: orderId={}, amount={}", order.getOrderId(), order.getTotalAmount());
        }

        if (order.getTotalAmount().compareTo(fraudCheckThreshold) > 0) {
            log.info("Running probabilistic fraud check for orderId={}", order.getOrderId());
            if (new Random().nextDouble() < 0.05) {
                log.error("Fraud check FAILED for orderId={}", order.getOrderId());
                throw new FraudDetectedException(FRAUD_DETECTED);
            }

            log.debug("Fraud check passed for orderId={}", order.getOrderId());
        }
    }

    private void validatePhysical(Order order, ProductDTO product, int quantity) {
        log.debug("Validating PHYSICAL item: productId={}, quantity={}, stock={}",
                product.getProductId(), quantity, product.getStockQuantity());

        if (product == null) {
            log.error("Cannot reserve inventory for null product");
            throw new WarehouseUnavailableException(WAREHOUSE_UNAVAILABLE);
        }

        if (product.getStockQuantity() == stockZero) {
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        if (!product.getActive()) {
            throw new OutOfStockException(OUT_OF_STOCK);
        }

        int newStock = product.getStockQuantity() - quantity;
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
        paymentCarriedOut(order.getTotalAmount());
        LocalDateTime deliveryDate = calculateDeliveryDate(order, product);
    }

    private void validateSubscription(Order order, ProductDTO product) {
        List<OrderResponse> orders = orderService.getOrdersByCustomer(order.getCustomerId());

        long subscriptionCount = orders.stream()
                .flatMap(orderItem -> orderItem.getItems().stream())
                .filter(item -> SUBSCRIPTION.name().equals(item.getProductType()))
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
                .anyMatch(item ->
                        SUBSCRIPTION.name().equals(item.getProductType()) &&
                                item.getProductId().equals(product.getProductId())
                );

        if (hasDuplicateActiveSubscription) {
            log.info("Duplicate active subscription for product {} ", hasDuplicateActiveSubscription);
            throw new DuplicateActiveSubscriptionException(DUPLICATE_ACTIVE_SUBSCRIPTION);
        }

        boolean hasSameProductInOrder = orderService.getOrdersByCustomer(order.getCustomerId()).stream()
                .filter(o -> o.getStatus() == OrderStatus.PROCESSED)
                .flatMap(o -> o.getItems().stream())
                .filter(item -> item.getProductType() == SUBSCRIPTION) // só assinaturas
                .anyMatch(item -> item.getProductId().equals(product.getProductId()));

        if (hasSameProductInOrder) {
            log.info("Incompatible subscription requested for product {} ", product.getProductId());
            throw new IncompatibleSubscriptionsException(INCOMPATIBLE_SUBSCRIPTIONS);
        }

        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toResponseItem)
                .collect(Collectors.toList());

        log.debug("Validating SUBSCRIPTION successfully productId={} for orderId={}", product.getProductId(), order.getOrderId());
        eventPublisher.publishEvent(OrderSchedulingPaymentEvent.of(order.getOrderId(), order.getCustomerId(), items, order.getTotalAmount()));
    }

    private void validateDigital(Order order, ProductDTO product) {
        log.debug("Validating DIGITAL productId={} for orderId={}", product.getProductId(), order.getOrderId());

        boolean clientAlreadyOwnsProduct =
                orderService.getOrdersByCustomer(order.getCustomerId()).stream()
                        .filter(o -> o.getStatus() == PROCESSED)
                        .flatMap(o -> o.getItems().stream())
                        .anyMatch(item ->
                                product.getProductId().equals(item.getProductId())
                        );

        if (clientAlreadyOwnsProduct) {
            throw new AlreadyOwnedDigitalProductException(ALREADY_OWNED);
        }

        int availableLicenses = getAvailableLicenses(product.getProductId());

        if (availableLicenses <= 0) {
            log.error("No licenses available for productId={}", product.getProductId());
            throw new LicenseUnavailableException(LICENSE_UNAVAILABLE);
        }

        String licenseKey = UUID.randomUUID().toString();
        log.info("Key active created={}", licenseKey);
        sendEmail(order.getOrderId(), licenseKey);
    }

    private void validatePreOrder(Order order, ProductDTO product) {
        log.debug("Validating PRE_ORDER productId={} for orderId={}",
                product.getProductId(), order.getOrderId());

        Optional<String> releaseDateRaw = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("releaseDate"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        releaseDateRaw.ifPresent(date -> {
            if (date != null) {
                LocalDate release = LocalDate.parse(date);
                log.debug("PRE_ORDER releaseDate={} for productId={}", release, product.getProductId());

                if (!release.isAfter(LocalDate.now())) {
                    log.error("RELEASE_DATE_PASSED for productId={} in orderId={}",
                            product.getProductId(), order.getOrderId());
                    throw new ReleaseDatePassedException(RELEASE_DATE_PASSED);
                }
            }
        });

        int requestedQuantity = order.getItems().stream()
                .filter(item -> item.getProductId().equals(product.getProductId()))
                .mapToInt(OrderItem::getQuantity)
                .sum();

        boolean reserved = productService.reservePreOrderSlots(product.getProductId(), requestedQuantity);

        if (!reserved) {
            throw new PreOrderSoldOutException(PRE_ORDER_SOLD_OUT);
        }

        log.info("Your order has been scheduled for delivery on {} ", releaseDateRaw);
    }

    private void validateCorporate(Order order, ProductDTO product, int qty) {
        log.debug("Validating CORPORATE productId={} for orderId={} qty={}", product.getProductId(), order.getOrderId(), qty);

        Optional<String> firstCnpj = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("cnpj"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        firstCnpj.ifPresent(cnpj -> {
            if (!CnpjValidator.isValidCnpj(cnpj)) {
                log.info("CNPJ invalid: {}", cnpj);
                throw new InvalidCorporateDataException(INVALID_CORPORATE_DATA);
            }
        });

        if (order.getTotalAmount().compareTo(new BigDecimal("100000")) > 0) {
            log.error("CREDIT_LIMIT_EXCEEDED for orderId={}", order.getOrderId());
            throw new CreditLimitExceededException(CREDIT_LIMIT_EXCEEDED);
        }

        int quantity = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        if (quantity > orporateVolumeThreshold) {
            BigDecimal total = order.getTotalAmount();
            BigDecimal discountRate = new BigDecimal(corporateDiscount);
            BigDecimal discount = total.multiply(discountRate);
            BigDecimal totalWithDiscount = total.subtract(discount).setScale(2, RoundingMode.HALF_UP);
            order.setTotalAmount(totalWithDiscount);
            log.info("Total without discount {} ", total);
            log.info("orderId={} volume discount applied: {}", order.getOrderId(), totalWithDiscount);
        }

        Optional<String> paymentTerms = order.getItems().stream()
                .map(OrderItem::getMetadata)
                .map(metadata -> metadata.get("paymentTerms"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst();

        paymentTerms.ifPresent(payment -> {
            calculateTermsPayment(payment);
        });

    }

    private LocalDateTime calculateDeliveryDate(Order order, ProductDTO product) {
        log.debug("Calculating delivery date for productId={}", product.getProductId());

        int deliveryDays = new Random().nextInt(6) + 5;

        LocalDateTime createdAt = order.getCreatedAt();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        LocalDateTime deliveryDate = createdAt.plusDays(deliveryDays);

        log.debug("Calculated delivery date: {} (in {} days from order creation)",
                deliveryDate, deliveryDays);

        return deliveryDate;
    }

    private void paymentCarriedOut(BigDecimal totalAmount) {
        log.info("Payment carried out successfully total={}", totalAmount);
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

    private int getAvailableLicenses(String productId) {
        return Math.abs(productId.hashCode() % 6);
    }

    private void sendEmail(String orderId, String licenseKey) {
        log.info("Send e-mail with product");
        String downloadLink = "https://download.fake.com/" + orderId;
        log.info("Your digital product is ready", "Download: " + downloadLink + "\nLicense: " + licenseKey);
    }

    private void calculateTermsPayment(String paymentTerms) {
        LocalDate dueDate = switch (paymentTerms) {
            case "NET_30" -> LocalDate.now().plusDays(30);
            case "NET_60" -> LocalDate.now().plusDays(60);
            case "NET_90" -> LocalDate.now().plusDays(90);
            default -> throw new InvalidPaymentTermsException(INVALID_PAYMENT_TERMS);
        };

        log.info("Due date {}", dueDate);
    }

}
