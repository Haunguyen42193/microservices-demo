package com.example.orderservices.consumer;

import com.example.orderservices.dto.ResponseData;
import com.example.orderservices.exceptions.InvalidOrderFormatException;
import com.example.orderservices.model.OrderEntity;
import com.example.orderservices.model.OrderLineItems;
import com.example.orderservices.repository.OrderLineItemRepository;
import com.example.orderservices.repository.OrderRepository;
import com.example.orderservices.service.OrderService;
import com.example.orderservices.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OrderConsumer {
    private final OrderRepository orderRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final RedisService redisService;
    private final OrderService orderService;

    @Autowired
    public OrderConsumer(OrderRepository orderRepository, OrderLineItemRepository orderLineItemRepository, RedisService redisService, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderLineItemRepository = orderLineItemRepository;
        this.redisService = redisService;
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.response.name}")
    public void consumeRabbitToDeleteOrder(ResponseData receive) {
        log.warn(String.format("Receive rabbit message delete order -> %s", receive.toString()));
        Integer orderId = (Integer) receive.getData();
        Long odId = Long.valueOf(orderId);
        log.info(odId.toString());
        log.info(odId.getClass().getName());
        OrderEntity orderEntity = orderRepository.findById(odId).orElse(null);
        if(orderEntity != null) {
            orderRepository.delete(orderEntity);
            redisService.deleteCache("order_" + orderEntity.getId().toString());
        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "demo-kafka-response",partitions = "1"), groupId = "cancelGroup")
    @Transactional
    public void consumeKafka(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //Lấy order nhận từ kafka
            OrderEntity orderEntityReceive = objectMapper.readValue(data, OrderEntity.class);
            List<OrderLineItems> orderItemsListReceive = orderEntityReceive.getOrderLineItemsList();

            log.warn("Receive kafka message rollback order-> " + orderEntityReceive.getId());

            //Lấy order trong db tương ứng với id của OrderReceive
            OrderEntity orderEntity = orderRepository.findById(orderEntityReceive.getId()).orElse(null);
            assert orderEntity != null;
            List<OrderLineItems> orderItemsList = orderEntity.getOrderLineItemsList();

            //Biến tmp
            List<OrderLineItems> orderLineItemsTmp = new ArrayList<>();
            boolean tmp;

            //Nếu item đã có trong db thì change quantity, nếu chưa có (item) rollback
            for (OrderLineItems orderItemReceive: orderItemsListReceive) {
                tmp = false;
                for (OrderLineItems items: orderItemsList) {
                    if(Objects.equals(items.getProductId(), orderItemReceive.getProductId()) && orderItemReceive.getQuantity() != 0) {
                        if (orderItemReceive.getQuantity() - items.getQuantity() == 0) {
                            orderLineItemsTmp.add(items);
                        }
                        items.setQuantity((items.getQuantity() - orderItemReceive.getQuantity()));
                        tmp = true;
                    }
                }
                if (!tmp && orderItemReceive.getQuantity() < 0) {
                    orderItemReceive.setQuantity(-orderItemReceive.getQuantity());
                    orderItemReceive.setOrder(orderEntity);
                    orderItemsList.add(orderItemReceive);
                }
            }

            //Xóa item thêm vào mà quantity quá
            List<OrderLineItems> orderItems = deleteOrderLineItem(orderLineItemsTmp, orderItemsList);

            orderEntity.setOrderLineItemsList(orderItems);
            redisService.updateCache("order_" + orderEntity.getId().toString(), orderService.mapToOrderResponse(orderEntity), (60*10));
            redisService.deleteCache("orders");
            orderRepository.save(orderEntity);

        } catch (JsonProcessingException e) {
            throw new InvalidOrderFormatException(e.getMessage());
        }
    }

    private List<OrderLineItems> deleteOrderLineItem(List<OrderLineItems> orderLineItemsTmp, List<OrderLineItems> orderItemsList) {
        if (!orderLineItemsTmp.isEmpty()) {
            for (OrderLineItems orderLineItems: orderLineItemsTmp) {
                orderItemsList.remove(orderLineItems);
                deleteOrderItemById(orderLineItems.getId());
            }
        }
        return orderItemsList;
    }

    private void deleteOrderItemById(long id) {
        orderLineItemRepository.deleteById(id);
    }
}
