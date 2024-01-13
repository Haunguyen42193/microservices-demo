package com.example.productservices.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Value("${kafka.response.topic.name}")
    private String topicResponse;

    @Bean
    public NewTopic newTopicResponse() { return TopicBuilder.name(topicResponse).partitions(2).build(); }
}
