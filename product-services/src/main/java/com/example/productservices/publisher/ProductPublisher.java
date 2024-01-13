package com.example.productservices.publisher;

import com.example.productservices.dto.OrderReceiveDto;
import com.example.productservices.dto.ResponseDataDto;
import com.example.productservices.exception.WriteJsonAsStringException;
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
public class ProductPublisher{
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.response.name}")
    private String routingKeyResponse;
    @Value("${kafka.response.topic.name}")
    private String topicName;
    private final KafkaTemplate<Object, String> kafkaTemplate;

    @Autowired
    public ProductPublisher(RabbitTemplate rabbitTemplate, KafkaTemplate<Object, String> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResponseMessage(ResponseDataDto response) {
        log.info("Send message -> " + response);
        rabbitTemplate.convertAndSend(exchange, routingKeyResponse, response);
    }

    public void sendCancelUpdateOrderMessage(OrderReceiveDto response){
        log.info("Send kafka message cancel update -> " + response);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new WriteJsonAsStringException(e.getMessage());
        }
        kafkaTemplate.send(topicName, 1, "demo-kafka", jsonString);
    }
}
