package com.example.productservices.consumer;

import com.example.productservices.dto.OrderLineItemsDto;
import com.example.productservices.dto.OrderReceiveDto;
import com.example.productservices.dto.ProductResponseDto;
import com.example.productservices.dto.ResponseDataDto;
import com.example.productservices.exception.InvalidOrderFormatException;
import com.example.productservices.model.ProductEntity;
import com.example.productservices.publisher.ProductPublisher;
import com.example.productservices.repository.ProductRepository;
import com.example.productservices.service.ProductService;
import com.example.productservices.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ProductConsumer {
    private final ProductRepository productRepository;
    private final ProductPublisher publisher;
    private final RedisService redisService;
    private final ProductService productService;
    private List<ProductEntity> productEntities;

    @Autowired
    public ProductConsumer(
            ProductRepository productRepository,
            ProductPublisher publisher,
            RedisService redisService,
            ProductService productService) {
        this.productRepository = productRepository;
        this.publisher = publisher;
        this.redisService = redisService;
        this.productService = productService;
    }

    //Place order -> decrease products quantity
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeJson(OrderReceiveDto receive) {
        log.info(String.format("Receive place order -> %s", receive.toString()));

        productEntities = getProductsFromOrder(receive.getOrderLineItemsList());
        if (productEntities.isEmpty()) {
            handleNotFoundProducts(receive);
            return;
        }

        if (!validateProductQuantities(receive, productEntities)) {
            handleInvalidQuantities(receive);
            return;
        }

        updateProductQuantities(productEntities, receive.getOrderLineItemsList());
        updateProductQuantityToCache(productEntities);
        productRepository.saveAll(productEntities);
    }

    private void handleNotFoundProducts(OrderReceiveDto receive) {
        publisher.sendResponseMessage(new ResponseDataDto(HttpStatus.NOT_FOUND, receive.getId().toString(), receive.getId()));
    }

    private boolean validateProductQuantities(OrderReceiveDto receive, List<ProductEntity> products) {
        for (ProductEntity productEntity : products) {
            for (OrderLineItemsDto productReceive : receive.getOrderLineItemsList()) {
                if (Objects.equals(productReceive.getProductId(), productEntity.getId()) &&
                        productEntity.getQuantity() - productReceive.getQuantity() < 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleInvalidQuantities(OrderReceiveDto receive) {
        publisher.sendResponseMessage(new ResponseDataDto(HttpStatus.BAD_REQUEST, receive.getId().toString(), receive.getId()));
    }

    private void updateProductQuantities(List<ProductEntity> products, List<OrderLineItemsDto> orderLineItems) {
        for (ProductEntity productEntity : products) {
            for (OrderLineItemsDto productReceive : orderLineItems) {
                if (Objects.equals(productReceive.getProductId(), productEntity.getId())) {
                    productEntity.setQuantity(productEntity.getQuantity() - productReceive.getQuantity());
                    break;
                }
            }
        }
    }


    //Delete order -> increase products quantity
    @RabbitListener(queues = "${rabbitmq.queue.delete.name}")
    public void consumeDeleteMessage(OrderReceiveDto receive) {
        log.info(String.format("Receive delete order -> %s", receive.toString()));
        productEntities = getProductsFromOrder(receive.getOrderLineItemsList());
        //Change products quantity
        for(ProductEntity productEntity : productEntities) {
            for (OrderLineItemsDto productReceive: receive.getOrderLineItemsList()) {
                if(Objects.equals(productReceive.getProductId(), productEntity.getId())) {
                    productEntity.setQuantity((productEntity.getQuantity() + productReceive.getQuantity()));
                    break;
                }
            }
        }
        updateProductQuantityToCache(productEntities);
        productRepository.saveAll(productEntities);
    }

    //Update order -> change products quantity
//    @KafkaListener(groupId = "myGroup", topicPartitions = @TopicPartition(topic = "${kafka.topic.name}", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "7")))
    @KafkaListener(groupId = "myGroup", topicPartitions = @TopicPartition(topic = "${kafka.topic.name}", partitions = "0"))
    public void consumeKafka(String order) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //Read value from messageJson received from kafka
            OrderReceiveDto orderReceiveDto = objectMapper.readValue(order, OrderReceiveDto.class);
            log.info("Receive kafka message -> " + orderReceiveDto.getId());
            //Get products from order (get on database)
            productEntities = getProductsFromOrder(orderReceiveDto.getOrderLineItemsList());
            if (productEntities.isEmpty()) {
                publisher.sendCancelUpdateOrderMessage(orderReceiveDto);
                return;
            }
            for (ProductEntity productEntity : productEntities) {
                for (OrderLineItemsDto productReceive: orderReceiveDto.getOrderLineItemsList()) {
                    if(Objects.equals(productReceive.getProductId(), productEntity.getId())) {
                        //If quantity change < 0 cancel and reply to Order service
                        if(productEntity.getQuantity() - productReceive.getQuantity() < 0){
                            publisher.sendCancelUpdateOrderMessage(orderReceiveDto);
                            return;
                        }
                        productEntity.setQuantity((productEntity.getQuantity() - productReceive.getQuantity()));
                        break;
                    }
                }
            }
            updateProductQuantityToCache(productEntities);
            productRepository.saveAll(productEntities);

        } catch (JsonProcessingException e) {
            throw new InvalidOrderFormatException(e.getMessage());
        }
    }

    private List<ProductEntity> getProductsFromOrder(List<OrderLineItemsDto> orderLineItems) {
        List<ProductEntity> products = new ArrayList<>();
        for (OrderLineItemsDto productReceive : orderLineItems) {
            ProductEntity product = productRepository.findById(productReceive.getProductId()).orElse(null);
            if (Objects.isNull(product))
                return Collections.emptyList();
            products.add(product);
        }
        return products;
    }

    private void updateProductQuantityToCache(List<ProductEntity> productEntities) {
        for (ProductEntity productEntity : productEntities) {
            String key = "product_" + productEntity.getId().toString();
            ProductResponseDto productResponseDto = productService.mapToResponse(productEntity);
            redisService.updateCache(key, productResponseDto, (60*10));
            redisService.deleteCache("products");
        }
    }
}
