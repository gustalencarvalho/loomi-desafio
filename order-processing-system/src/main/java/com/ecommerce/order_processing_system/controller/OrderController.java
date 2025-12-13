package com.ecommerce.order_processing_system.controller;

import com.ecommerce.order_processing_system.dto.CreateOrderRequest;
import com.ecommerce.order_processing_system.dto.OrderResponse;
import com.ecommerce.order_processing_system.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request) {
        log.info("Request POST /api/orders [BODY]: {} ", request);
        OrderResponse response = service.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String orderId) {
        log.info("Request GET /api/orders/{}", orderId);
        return ResponseEntity.ok(service.getOrder(orderId));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listByCustomer(@RequestParam String customerId) {
        log.info("Request GET /api/orders?customerId={}", customerId);
        return ResponseEntity.ok(service.getOrdersByCustomer(customerId));
    }
}
