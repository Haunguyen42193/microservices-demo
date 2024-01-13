package com.example.orderservices.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.queue.name}")
    private String queue;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.routing.name}")
    private String routingKey;

    @Value("${rabbitmq.queue.response.name}")
    private String queueResponse;
    @Value("${rabbitmq.routing.response.name}")
    private String routingKeyResponse;

    @Value("${rabbitmq.routing.delete.name}")
    private String routingDeleteKey;
    @Value("${rabbitmq.queue.delete.name}")
    private String queueDelete;

    @Bean
    public Queue queue() { return new Queue(queue); }

    @Bean
    public TopicExchange exchange() { return new TopicExchange(exchange); }

    @Bean
    public Queue queueResponse() { return new Queue(queueResponse); }

    @Bean
    public Queue queueDelete() { return new Queue(queueDelete); }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(routingKey);
    }

    @Bean
    public Binding bindingResponse() {
        return BindingBuilder
                .bind(queueResponse())
                .to(exchange())
                .with(routingKeyResponse);
    }

    @Bean
    public Binding bindingDelete() {
        return BindingBuilder
                .bind(queueDelete())
                .to(exchange())
                .with(routingDeleteKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setExchange(exchange);
        return rabbitTemplate;
    }
}
