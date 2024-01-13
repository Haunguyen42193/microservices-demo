package com.example.orderservices.service;

import com.example.orderservices.dto.OrderRequest;
import com.example.orderservices.dto.OrderResponse;
import com.example.orderservices.model.OrderEntity;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getOrders();

    OrderResponse getOrderById(Long id);

    OrderResponse placeOrder(OrderRequest orderRequest);

    void deleteOrder(Long id);

    OrderResponse updateOrder(Long id, OrderRequest orderRequest);

    OrderResponse mapToOrderResponse(OrderEntity orderEntity);

    List<OrderResponse> getOrdersByOrderLineItemQuantityLessThanEqual(int quantity);

    List<OrderResponse> getOrdersByOrderLineItemQuantityProductId(Long productId);

    Integer getTotalQuantityById(Long productId);

    BigDecimal getTotalPriceById(Long id, String authorizationHeader);
}
