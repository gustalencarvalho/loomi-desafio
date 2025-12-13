package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.OrderStatus;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderItemResponse;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.events.OrderCreatedEvent;
import com.ecommerce.order_processing_system.exception.ErrorSystemException;
import com.ecommerce.order_processing_system.exception.OrderNotFoundException;
import com.ecommerce.order_processing_system.exception.QuantityInvalidException;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                productService,
                objectMapper,
                eventPublisher
        );
    }

    // ========== createOrder Tests ==========

    @Test
    void testCreateOrderSuccess() {
        // Arrange
        String customerId = "cust-001";
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(customerId)
                .items(Arrays.asList(itemRequest))
                .build();

        ProductDTO product = ProductDTO.builder()
                .productId("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(10)
                .productType(ProductType.PHYSICAL)
                .active(true)
                .metadata(null)
                .build();

        Order savedOrder = Order.builder()
                .orderId("ORD-001")
                .customerId(customerId)
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(productService.getProductOrThrow("PROD-001")).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals("ORD-001", response.getOrderId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), response.getTotalAmount());

        verify(productService).getProductOrThrow("PROD-001");
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void testCreateOrderWithMultipleItems() {
        // Arrange
        CreateOrderItemRequest item1 = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(1)
                .build();

        CreateOrderItemRequest item2 = CreateOrderItemRequest.builder()
                .productId("PROD-002")
                .quantity(3)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust-001")
                .items(Arrays.asList(item1, item2))
                .build();

        ProductDTO product1 = ProductDTO.builder()
                .productId("PROD-001")
                .name("Product 1")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(10)
                .active(true)
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .productId("PROD-002")
                .name("Product 2")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(20)
                .active(true)
                .build();

        OrderItem savedItem = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Test Product")
                .productType(ProductType.PHYSICAL)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(250.00))
                .metadata(null)
                .build();

        Order savedOrder = Order.builder()
                .orderId("ORD-001")
                .customerId("customerId")
                .items(Arrays.asList(savedItem))
                .totalAmount(BigDecimal.valueOf(250.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();


        when(productService.getProductOrThrow("PROD-001")).thenReturn(product1);
        when(productService.getProductOrThrow("PROD-002")).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(250.00), response.getTotalAmount());
        verify(productService, times(2)).getProductOrThrow(anyString());
    }

    @Test
    void testCreateOrderWithZeroQuantity() {
        // Arrange
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(0)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust-001")
                .items(Arrays.asList(itemRequest))
                .build();

        // Act & Assert
        assertThrows(QuantityInvalidException.class,
                () -> orderService.createOrder(request));

        verify(productService, never()).getProductOrThrow(anyString());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCreateOrderWithNegativeQuantity() {
        // Arrange
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(-5)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust-001")
                .items(Arrays.asList(itemRequest))
                .build();

        // Act & Assert
        assertThrows(QuantityInvalidException.class,
                () -> orderService.createOrder(request));

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ========== getOrder Tests ==========

    @Test
    void testGetOrderSuccess() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Test Product")
                .productType(ProductType.PHYSICAL)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(200.00))
                .metadata(null)
                .build();

        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(item))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(OrderStatus.PROCESSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById("ORD-001")).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrder("ORD-001");

        // Assert
        assertNotNull(response);
        assertEquals("ORD-001", response.getOrderId());
        assertEquals("cust-001", response.getCustomerId());
        assertEquals(OrderStatus.PROCESSED, response.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void testGetOrderNotFound() {
        // Arrange
        when(orderRepository.findById("INVALID-ID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrder("INVALID-ID"));
    }

    // ========== getOrdersByCustomer Tests ==========

    @Test
    void testGetOrdersByCustomerSuccess() {
        // Arrange
        Order order1 = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .orderId("ORD-002")
                .customerId("cust-001")
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(OrderStatus.PROCESSED)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByCustomerId("cust-001"))
                .thenReturn(Arrays.asList(order1, order2));

        // Act
        List<OrderResponse> responses = orderService.getOrdersByCustomer("cust-001");

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("ORD-001", responses.get(0).getOrderId());
        assertEquals("ORD-002", responses.get(1).getOrderId());
        assertEquals(BigDecimal.valueOf(100.00), responses.get(0).getTotalAmount());
        assertEquals(BigDecimal.valueOf(200.00), responses.get(1).getTotalAmount());
    }

    @Test
    void testGetOrdersByCustomerEmpty() {
        // Arrange
        when(orderRepository.findByCustomerId("cust-empty"))
                .thenReturn(Collections.emptyList());

        // Act
        List<OrderResponse> responses = orderService.getOrdersByCustomer("cust-empty");

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    // ========== toOrderItem Tests ==========

    @Test
    void testToOrderItemSuccess() {
        // Arrange
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(3)
                .build();

        ProductDTO product = ProductDTO.builder()
                .productId("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(100)
                .productType(ProductType.DIGITAL)
                .metadata(null)
                .build();

        when(productService.getProductOrThrow("PROD-001")).thenReturn(product);

        // Act
        OrderItem item = orderService.toOrderItem(itemRequest);

        // Assert
        assertNotNull(item);
        assertEquals("PROD-001", item.getProductId());
        assertEquals("Test Product", item.getProductName());
        assertEquals(3, item.getQuantity());
        assertEquals(BigDecimal.valueOf(50.00), item.getPrice());
        assertEquals(BigDecimal.valueOf(150.00), item.getSubtotal()); // 50 * 3
        assertEquals(ProductType.DIGITAL, item.getProductType());
    }

    @Test
    void testToOrderItemInvalidQuantity() {
        // Arrange
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(0)
                .build();

        // Act & Assert
        assertThrows(QuantityInvalidException.class,
                () -> orderService.toOrderItem(itemRequest));

        verify(productService, never()).getProductOrThrow(anyString());
    }

    // ========== toResponse Tests ==========

    @Test
    void testToResponseSuccess() {
        // Arrange
        OrderItem item = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Product Name")
                .productType(ProductType.PHYSICAL)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(200.00))
                .metadata(null)
                .build();

        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(item))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(OrderStatus.PENDING)
                .failureReason(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        OrderResponse response = orderService.toResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals("ORD-001", response.getOrderId());
        assertEquals("cust-001", response.getCustomerId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(200.00), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertNull(response.getFailureReason());
    }

    @Test
    void testToResponseWithFailureReason() {
        // Arrange
        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.FAILED)
                .failureReason("OUT_OF_STOCK")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        OrderResponse response = orderService.toResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.FAILED, response.getStatus());
        assertEquals("OUT_OF_STOCK", response.getFailureReason());
    }

    @Test
    void testToResponseWithMetadata() throws JsonProcessingException {
        // Arrange
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("key", "value");
        metadataMap.put("color", "red");

        OrderItem item = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Product")
                .productType(ProductType.PHYSICAL)
                .quantity(1)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(100.00))
                .metadata(metadataMap)
                .build();

        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(item))
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(objectMapper.writeValueAsString(metadataMap))
                .thenReturn("{\"key\": \"value\", \"color\": \"red\"}");

        // Act
        OrderResponse response = orderService.toResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertNotNull(response.getItems().get(0).getMetadata());
        assertEquals("{\"key\": \"value\", \"color\": \"red\"}",
                response.getItems().get(0).getMetadata());
    }


    @Test
    void testToResponseMetadataSerializationError() throws JsonProcessingException {
        // Arrange
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("key", "value");

        OrderItem item = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Product")
                .productType(ProductType.PHYSICAL)
                .quantity(1)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(100.00))
                .metadata(metadataMap)
                .build();

        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(item))
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(objectMapper.writeValueAsString(metadataMap))
                .thenThrow(new RuntimeException("JSON error"));

        // Act & Assert
        assertThrows(ErrorSystemException.class,
                () -> orderService.toResponse(order));
    }


    @Test
    void testToResponseWithMultipleItems() {
        // Arrange
        OrderItem item1 = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Product 1")
                .productType(ProductType.PHYSICAL)
                .quantity(1)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(100.00))
                .metadata(null)
                .build();

        OrderItem item2 = OrderItem.builder()
                .itemId("ITEM-002")
                .productId("PROD-002")
                .productName("Product 2")
                .productType(ProductType.DIGITAL)
                .quantity(2)
                .price(BigDecimal.valueOf(50.00))
                .subtotal(BigDecimal.valueOf(100.00))
                .metadata(null)
                .build();

        Order order = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(item1, item2))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(OrderStatus.PROCESSED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        OrderResponse response = orderService.toResponse(order);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getItems().size());
        assertEquals("PROD-001", response.getItems().get(0).getProductId());
        assertEquals("PROD-002", response.getItems().get(1).getProductId());
    }

    // ========== Event Publishing Tests ==========

    @Test
    void testOrderCreatedEventPublished() {
        // Arrange
        CreateOrderItemRequest itemRequest = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(1)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust-001")
                .items(Arrays.asList(itemRequest))
                .build();

        ProductDTO product = ProductDTO.builder()
                .productId("PROD-001")
                .name("Product")
                .price(BigDecimal.valueOf(100.00))
                .active(true)
                .build();

        OrderItem savedItem = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Test Product")
                .productType(ProductType.PHYSICAL)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(250.00))
                .metadata(null)
                .build();

        Order savedOrder = Order.builder()
                .orderId("ORD-001")
                .customerId("cust-001")
                .items(Arrays.asList(savedItem))
                .totalAmount(BigDecimal.valueOf(250.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();


        when(productService.getProductOrThrow("PROD-001")).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        orderService.createOrder(request);

        // Assert
        ArgumentCaptor<OrderCreatedEvent> captor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        OrderCreatedEvent event = captor.getValue();
        assertNotNull(event);
        assertEquals("ORD-001", event.getPayload().getOrderId());
        assertEquals("cust-001", event.getPayload().getCustomerId());
    }

    // ========== Total Amount Calculation Tests ==========

    @Test
    void testCreateOrderTotalAmountCalculation() {
        // Arrange
        CreateOrderItemRequest item1 = CreateOrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();

        CreateOrderItemRequest item2 = CreateOrderItemRequest.builder()
                .productId("PROD-002")
                .quantity(3)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("cust-001")
                .items(Arrays.asList(item1, item2))
                .build();

        ProductDTO product1 = ProductDTO.builder()
                .productId("PROD-001")
                .name("P1")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();

        ProductDTO product2 = ProductDTO.builder()
                .productId("PROD-002")
                .name("P2")
                .price(BigDecimal.valueOf(30.00))
                .active(true)
                .build();

        // Expected: (50 * 2) + (30 * 3) = 100 + 90 = 190
        OrderItem savedItem = OrderItem.builder()
                .itemId("ITEM-001")
                .productId("PROD-001")
                .productName("Test Product")
                .productType(ProductType.PHYSICAL)
                .quantity(2)
                .price(BigDecimal.valueOf(100.00))
                .subtotal(BigDecimal.valueOf(200.00))
                .metadata(null)
                .build();

        Order savedOrder = Order.builder()
                .orderId("ORD-001")
                .customerId("customerId")
                .items(Arrays.asList(savedItem))
                .totalAmount(BigDecimal.valueOf(190.00))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();


        when(productService.getProductOrThrow("PROD-001")).thenReturn(product1);
        when(productService.getProductOrThrow("PROD-002")).thenReturn(product2);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(request);

        // Assert
        assertEquals(BigDecimal.valueOf(190.00), response.getTotalAmount());
    }
}