package com.example.orderservices.service.impl;

import com.example.orderservices.dto.*;
import com.example.orderservices.exceptions.OrderNotFoundException;
import com.example.orderservices.model.OrderEntity;
import com.example.orderservices.model.OrderLineItems;
import com.example.orderservices.publisher.OrderPublisher;
import com.example.orderservices.repository.OrderLineItemRepository;
import com.example.orderservices.repository.OrderRepository;
import com.example.orderservices.service.OrderService;
import com.example.orderservices.service.RedisService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final OrderPublisher publisher;
    private final RedisService redisService;
    private final RestTemplate restTemplate;
    private static final String ORDER = "order_";
    private static final String ORDERS = "orders";
    @Value("${application.host}")
    private String host;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderLineItemRepository orderLineItemRepository, OrderPublisher publisher, RedisService redisService, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.publisher = publisher;
        this.redisService = redisService;
        this.restTemplate = restTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OrderResponse> getOrders() {
        List<OrderResponse> orderResponses = (List<OrderResponse>) redisService.getFromCache(ORDERS);

        if (orderResponses != null) {
            return orderResponses;
        }
        List<OrderEntity> orderEntities = orderRepository.findAll();
        orderResponses = orderEntities.stream().map(this::mapToOrderResponse).toList();
        redisService.addToCache(ORDERS, orderResponses, (60 * 10));
        return orderResponses;
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        OrderResponse orderResponse = (OrderResponse) redisService.getFromCache("product" + id.toString());

        if (orderResponse != null) {
            return orderResponse;
        }
        orderResponse = orderRepository.findById(id).map(this::mapToOrderResponse).orElse(null);
        if (orderResponse != null) {
            redisService.addToCache(ORDER + id, orderResponse, (60 * 10));
            return orderResponse;
        } else {
            throw new OrderNotFoundException("Not found this order");
        }
    }

    @Override
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        OrderEntity orderEntity = new OrderEntity();
        List<OrderLineItems> list = orderRequest.getOrderLineItemsListDto().stream().map(this::mapToOrItems).toList();
        for (OrderLineItems orderLineItems : list) {
            orderLineItems.setOrder(orderEntity);
        }
        orderEntity.setOrderLineItemsList(list);
        orderRepository.save(orderEntity);
        publisher.sendOrderMessage(orderEntity);
        OrderResponse orderResponse = mapToOrderResponse(orderEntity);
        log.info("place order -> " + orderResponse.getId().toString());
        redisService.addToCache(ORDER + orderEntity.getId().toString(), orderResponse, (60 * 10));
        redisService.deleteCache(ORDERS);
        return orderResponse;
    }

    @Override
    public void deleteOrder(Long id) {
        OrderEntity orderEntity = orderRepository.findById(id).orElse(null);
        if (orderEntity != null) {
            log.info("Delete order " + id);
            orderRepository.delete(orderEntity);
            publisher.sendDeleteOrderMessage(orderEntity);
            redisService.deleteCache(ORDER + id);
            redisService.deleteCache(ORDERS);
        } else {
            throw new OrderNotFoundException("Order not found " + id);
        }
    }

    @Override
    public OrderResponse updateOrder(Long id, OrderRequest orderRequest) {
        OrderEntity orderEntity = orderRepository.findById(id).orElse(null);
        if (orderEntity != null) {
            OrderEntity orderEntityToSend = OrderEntity.builder().id(orderEntity.getId())
                    .orderLineItemsList(mapToListItemToSend(orderRequest.getOrderLineItemsListDto(), orderEntity))
                    .build();
            //Gán lại Order trong OrderLineItem
            for (OrderLineItems orderLineItems : orderEntityToSend.getOrderLineItemsList()) {
                orderLineItems.setOrder(orderEntity);
            }
            orderEntity.setOrderLineItemsList(mapToListItemToSave(orderRequest.getOrderLineItemsListDto(), orderEntity));
            for (OrderLineItems orderLineItems : orderEntity.getOrderLineItemsList()) {
                orderLineItems.setOrder(orderEntity);
            }
            orderRepository.save(orderEntity);
            publisher.sendUpdateOrderMessage(orderEntityToSend);
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            redisService.updateCache(ORDER + orderEntity.getId().toString(), orderResponse, (60 * 10));
            redisService.deleteCache(ORDERS);
            return orderResponse;
        } else {
            throw new OrderNotFoundException("Not found this order");
        }
    }

    @Override
    public OrderResponse mapToOrderResponse(OrderEntity orderEntity) {
        return OrderResponse.builder().id(orderEntity.getId())
                .orderLineItemDtos(orderEntity.getOrderLineItemsList().stream()
                        .map(this::mapToOrderLineItemDto).toList()).build();
    }

    @Override
    public List<OrderResponse> getOrdersByOrderLineItemQuantityLessThanEqual(int quantity) {
        List<OrderEntity> list = orderRepository.findOrderEntitiesByOrderLineItemsList_QuantityLessThanEqual(quantity);
        return list.stream().map(this::mapToOrderResponse).toList();
    }

    @Override
    public List<OrderResponse> getOrdersByOrderLineItemQuantityProductId(Long productId) {
        List<OrderEntity> list = orderRepository.findOrderEntitiesByOrderLineItemsList_ProductId(productId);
        return list.stream().map(this::mapToOrderResponse).toList();
    }

    @Override
    public Integer getTotalQuantityById(@NonNull Long id) {
        Integer total = orderRepository.sumOrderEntity_OrderLineItemsList_QuantityById(id);
        if (Objects.isNull(total)) throw new OrderNotFoundException("Not found order with id -> " + id);
        return total;
    }

    @Override
    public BigDecimal getTotalPriceById(Long id, String authorizationHeader) {
        String url = "http://" + host + ":8082/api/product/get-total-price?order-id={orderId}";
        OrderEntity orderEntity = orderRepository.findById(id).orElse(null);
        if (Objects.isNull(orderEntity)) throw new OrderNotFoundException("Not found order with id -> " + id);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.setBearerAuth(authorizationHeader.substring(7));
        HttpEntity<OrderEntity> entity = new HttpEntity<>(orderEntity, headers);
        ResponseEntity<BigDecimal> response = restTemplate.exchange(url, HttpMethod.GET, entity, BigDecimal.class, orderEntity.getId());
        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Total price -> " + response.getBody());
            return response.getBody();
        }
        return null;
    }

    private OrderLineItems mapToOrItems(OrderLineItemDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .productId(orderLineItemsDto.getProductId())
                .quantity(orderLineItemsDto.getQuantity()).build();
    }

    private List<OrderLineItems> mapToListItemToSend(List<OrderLineItemDto> orderLineItemDtoList, OrderEntity orderEntity) {
        List<OrderLineItems> orderLineItemsList;
        boolean tmp;
        orderLineItemsList = isOnItemList(orderLineItemDtoList, orderEntity);
        for (OrderLineItemDto dto : orderLineItemDtoList) {
            tmp = false;
            for (OrderLineItems orderLineItems : orderEntity.getOrderLineItemsList())
                if (Objects.equals(dto.getProductId(), orderLineItems.getProductId())) {
                    OrderLineItems items = mapToOrItems(dto);
                    items.setId(orderLineItems.getId());
                    items.setQuantity(dto.getQuantity() - orderLineItems.getQuantity());
                    orderLineItemsList.add(items);
                    tmp = true;
                    break;
                }
            if (!tmp) {
                orderLineItemsList.add(mapToOrItems(dto));
            }
        }
        return orderLineItemsList;
    }

    private List<OrderLineItems> isOnItemList(List<OrderLineItemDto> orderLineItemDtoList, OrderEntity orderEntity) {
        boolean tmp;
        List<OrderLineItems> orderLineItemsList = new ArrayList<>();
        for (OrderLineItems orderLineItems : orderEntity.getOrderLineItemsList()) {
            tmp = false;
            for (OrderLineItemDto dto : orderLineItemDtoList)
                if (Objects.equals(dto.getProductId(), orderLineItems.getProductId())) {
                    tmp = true;
                    break;
                }
            if (!tmp) {
                orderLineItemsList.add(OrderLineItems.builder().productId(orderLineItems.getProductId())
                        .quantity(-orderLineItems.getQuantity()).build());
            }
        }
        return orderLineItemsList;
    }

    private List<OrderLineItems> mapToListItemToSave(List<OrderLineItemDto> orderLineItemDtoList, OrderEntity orderEntity) {
        List<OrderLineItems> orderLineItemsList = new ArrayList<>();
        boolean tmp;
        for (OrderLineItemDto dto : orderLineItemDtoList) {
            tmp = false;
            for (OrderLineItems orderLineItems : orderEntity.getOrderLineItemsList())
                if (Objects.equals(dto.getProductId(), orderLineItems.getProductId())) {
                    OrderLineItems items = mapToOrItems(dto);
                    items.setId(orderLineItems.getId());
                    orderLineItemsList.add(items);
                    tmp = true;
                    break;
                }
            if (!tmp && dto.getQuantity() != 0) {
                orderLineItemsList.add(mapToOrItems(dto));
            }
        }
        deleteOrderItem(orderEntity, orderLineItemsList);
        return orderLineItemsList;
    }

    private OrderLineItemDto mapToOrderLineItemDto(OrderLineItems orderLineItems) {
        return OrderLineItemDto.builder().productId(orderLineItems.getProductId())
                .quantity(orderLineItems.getQuantity()).build();
    }

    private void deleteOrderItem(OrderEntity orderEntity, List<OrderLineItems> orderLineItemsList) {
        boolean tmp;
        for (OrderLineItems orderItem : orderEntity.getOrderLineItemsList()) {
            tmp = false;
            for (OrderLineItems dto : orderLineItemsList)
                if (Objects.equals(dto.getProductId(), orderItem.getProductId())) {
                    tmp = true;
                    break;
                }
            if (!tmp && orderLineItemRepository.findById(orderItem.getId()).orElse(null) != null) {
                orderLineItemRepository.deleteById(orderItem.getId());
            }
        }
    }
}