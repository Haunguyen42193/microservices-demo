package com.example.orderservices.publisher;

import com.example.orderservices.exceptions.InvalidOrderFormatException;
import com.example.orderservices.model.OrderEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPublisher {
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.name}")
    private String routingKey;
    @Value("${rabbitmq.routing.delete.name}")
    private String routingDeleteKey;
    @Value("${kafka.topic.name}")
    private String topicName;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OrderPublisher(KafkaTemplate<String, String> kafkaTemplate, RabbitTemplate rabbitTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderMessage(OrderEntity orderEntity) {
        log.info("Send message -> " + orderEntity);
        rabbitTemplate.convertAndSend(exchange, routingKey, orderEntity);
    }

    public void sendDeleteOrderMessage(OrderEntity orderEntity) {
        log.info("Send message delete -> " + orderEntity);
        rabbitTemplate.convertAndSend(exchange, routingDeleteKey, orderEntity);
    }

    public void sendUpdateOrderMessage(OrderEntity orderEntity){
        log.info("Send kafka message update -> " + orderEntity);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(orderEntity);
        } catch (JsonProcessingException e) {
            throw new InvalidOrderFormatException(e.getMessage());
        }
        kafkaTemplate.send(topicName, 0, "demo-kafka", jsonString);
    }
}
