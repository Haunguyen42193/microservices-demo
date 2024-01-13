package com.example.orderservices.controller;

import com.example.orderservices.dto.OrderRequest;
import com.example.orderservices.dto.OrderResponse;
import com.example.orderservices.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.placeOrder(orderRequest);
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<OrderResponse> responses = orderService.getOrders();
        if (!responses.isEmpty())
            return ResponseEntity.ok(responses);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        if (orderResponse != null)
            return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable Long id, @RequestBody OrderRequest orderRequest) {
        log.info("Body: " + orderRequest);
        OrderResponse orderResponse = orderService.updateOrder(id, orderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponse);
    }

    @GetMapping("/get-orders-by-quantity-less-than-equal")
    public ResponseEntity<List<OrderResponse>> getOrderByOrderLineItemQuantityLessThanEqual(@RequestParam int quantity) {
        log.info("Param quantity -> " + quantity);
        List<OrderResponse> orderResponses = orderService.getOrdersByOrderLineItemQuantityLessThanEqual(quantity);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponses);
    }

    @GetMapping("/get-orders-by-product-id")
    public ResponseEntity<List<OrderResponse>> getOrderByOrderLineItemProductId(@RequestParam Long productId) {
        log.info("Param productId-> " + productId);
        List<OrderResponse> orderResponses = orderService.getOrdersByOrderLineItemQuantityProductId(productId);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponses);
    }

    @GetMapping("/get-total-quantity/{id}")
    public ResponseEntity<Integer> getTotalQuantityByProductId(@PathVariable Long id) {
        log.info("Param -> " + id);
        Integer orderResponses = orderService.getTotalQuantityById(id);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponses);
    }

    @GetMapping("/get-total-price/{id}")
    public ResponseEntity<BigDecimal> getTotalPriceByProductId(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id) {
        log.info("Param -> " + id);
        BigDecimal orderResponses = orderService.getTotalPriceById(id, authorizationHeader);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponses);
    }
}
