package com.ecommerce.order_processing_system.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.dto.CreateOrderItemRequest;
import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.dto.ProductDTO;
import com.ecommerce.order_processing_system.exception.OrderNotFoundException;
import com.ecommerce.order_processing_system.exception.OutOfStockException;
import com.ecommerce.order_processing_system.kafka.KafkaEventPublisher;
import com.ecommerce.order_processing_system.kafka.events.OrderCreatedEvent;
import com.ecommerce.order_processing_system.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.ecommerce.order_processing_system.domain.OrderStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private ProductDTO product;

    @BeforeEach
    void setUp() {
        product = new ProductDTO();
        product.setProductId("PROD-1");
        product.setName("Product 1");
        product.setPrice(new BigDecimal("10"));
        product.setStockQuantity(10);
        product.setProductType(ProductType.PHYSICAL);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setProductId("PROD-1");
        itemRequest.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId("CUSTOMER-1");
        request.setItems(List.of(itemRequest));

        // Mock do productService
        when(productService.getProductOrThrow("PROD-1")).thenReturn(product);

        // Mock do save
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setOrderId("ORDER-1");
            return saved;
        });

        // Mock do flush (não faz nada, só para não lançar exceção)
        doNothing().when(orderRepository).flush();

        OrderResponse response = orderService.createOrder(request);

        // Validações
        assertNotNull(response);
        assertEquals("ORDER-1", response.getOrderId());
        assertEquals(PENDING, response.getStatus());
        assertEquals(new BigDecimal("20"), response.getTotalAmount());

        // Verifica interações
        verify(orderRepository).save(any(Order.class));
        verify(orderRepository).flush();
        verify(eventPublisher).publishCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void shouldThrowOutOfStockWhenQuantityExceedsStock() {
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setProductId("PROD-1");
        itemRequest.setQuantity(20);

        when(productService.getProductOrThrow("PROD-1")).thenReturn(product);

        assertThrows(
                OutOfStockException.class,
                () -> orderService.toOrderItem(itemRequest)
        );
    }

    @Test
    void shouldReturnOrderWhenExists() {
        Order order = Order.builder()
                .orderId("ORDER-1")
                .customerId("CUSTOMER-1")
                .status(PENDING)
                .items(List.of())
                .totalAmount(BigDecimal.TEN)
                .build();

        when(orderRepository.findById("ORDER-1")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrder("ORDER-1");

        assertEquals("ORDER-1", response.getOrderId());
        verify(orderRepository).findById("ORDER-1");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById("ORDER-404")).thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrder("ORDER-404")
        );
    }

    @Test
    void shouldReturnOrdersByCustomer() {
        Order order1 = Order.builder()
                .orderId("O1")
                .items(List.of())
                .build();

        Order order2 = Order.builder()
                .orderId("O2")
                .items(List.of())
                .build();

        when(orderRepository.findByCustomerId("CUSTOMER-1"))
                .thenReturn(List.of(order1, order2));

        List<OrderResponse> orders = orderService.getOrdersByCustomer("CUSTOMER-1");

        assertEquals(2, orders.size());
        verify(orderRepository).findByCustomerId("CUSTOMER-1");
    }

}
