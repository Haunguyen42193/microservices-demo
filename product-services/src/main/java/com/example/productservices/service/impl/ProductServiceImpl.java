package com.example.productservices.service.impl;

import com.example.productservices.dto.*;
import com.example.productservices.exception.OrderNotFoundException;
import com.example.productservices.exception.ProductNotFoundException;
import com.example.productservices.model.OrderEntity;
import com.example.productservices.model.OrderLineItems;
import com.example.productservices.model.ProductEntity;
import com.example.productservices.repository.OrderRepository;
import com.example.productservices.repository.ProductRepository;
import com.example.productservices.service.ProductService;
import com.example.productservices.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final RedisService redisService;
    private static final String PRODUCTS = "products";
    private static final String PRODUCT = "product_";
    private static final String PRODUCT_NOT_FOUND = "Product not found with id: ";

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, OrderRepository orderRepository, RedisService redisService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.redisService = redisService;
    }


    public List<ProductResponseDto> getAllProduct() {
        List<ProductResponseDto> productResponseDtos = (List<ProductResponseDto>) redisService.getFromCache(PRODUCTS);
        if (productResponseDtos != null)
            return productResponseDtos;
        List<ProductEntity> productEntities = productRepository.findAll();
        productResponseDtos = productEntities.stream().map(this::mapToResponse).toList();
        redisService.addToCache(PRODUCTS, productResponseDtos, (60*10));
        return productResponseDtos;
    }

    public ProductResponseDto getProductById(Long id) {
        ProductResponseDto productResponseDto = (ProductResponseDto) redisService.getFromCache(PRODUCTS + id.toString());
        if(productResponseDto != null) return productResponseDto;
        ProductEntity productEntity = productRepository.findById(id).orElse(null);
        if(productEntity != null)
        {
            log.info("Product get " + productEntity.getId());
            productResponseDto = mapToResponse(productEntity);
            redisService.addToCache(PRODUCT + productEntity.getId().toString(), productResponseDto, (60*10));
            return productResponseDto;
        }
        throw new ProductNotFoundException(PRODUCT_NOT_FOUND + id);
    }

    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        ProductEntity productEntity = ProductEntity.builder().
                name(productRequestDto.getName())
                .descriptions(productRequestDto.getDescriptions())
                .price(productRequestDto.getPrice())
                .quantity(productRequestDto.getQuantity()).build();
        productRepository.save(productEntity);
        log.info(String.format("Create product %d", productEntity.getId()));
        ProductResponseDto productResponseDto = mapToResponse(productEntity);
        redisService.addToCache(PRODUCT + productEntity.getId().toString(), productResponseDto, (60*10));
        redisService.deleteCache(PRODUCTS);
        return productResponseDto;
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
        ProductEntity productEntity = productRepository.findById(id).map(productEntity1 -> {
            productEntity1.setName(productRequestDto.getName());
            productEntity1.setDescriptions(productRequestDto.getDescriptions());
            productEntity1.setPrice(productRequestDto.getPrice());
            productEntity1.setQuantity(productRequestDto.getQuantity());
            return productRepository.save(productEntity1);
        }).orElse(null);
        if(productEntity != null)
        {
            log.info("Product update " + productEntity.getId());
            ProductResponseDto productResponseDto = mapToResponse(productEntity);
            redisService.updateCache(PRODUCT + productEntity.getId().toString(), productResponseDto, (60*10));
            redisService.deleteCache(PRODUCTS);
            return productResponseDto;
        } else {
            throw new ProductNotFoundException(PRODUCT_NOT_FOUND + id);
        }
    }

    public void deleteProduct(Long id) {
        ProductEntity p = productRepository.findById(id).orElse(null);
        if(p != null)
        {
            log.info("Product delete " + p.getId());
            productRepository.delete(p);
            redisService.deleteCache(PRODUCT + id);
            redisService.deleteCache(PRODUCTS);
        } else {
            throw new ProductNotFoundException(PRODUCT_NOT_FOUND + id);
        }
    }

    public ProductResponseDto mapToResponse(ProductEntity productEntity) {
        return ProductResponseDto.builder().name(productEntity.getName())
                .id(productEntity.getId())
                .descriptions(productEntity.getDescriptions())
                .price(productEntity.getPrice())
                .quantity(productEntity.getQuantity())
                .build();
    }

    @Override
    public List<ProductResponseDto> getByNameId(ProductNameIdDto productNameIdDto) {
        List<ProductResponseDto> list;
        List<ProductEntity> productEntities = productRepository
                .findProductEntitiesByNameAndPrice(productNameIdDto.getName(), productNameIdDto.getPrice());
        list = productEntities.stream().map(this::mapToResponse).toList();
        return list;
    }

    @Override
    public BigDecimal getTotalPrice(Long orderId) {
        log.info("Get orderId -> " + orderId);
        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (Objects.isNull(order))
            throw new OrderNotFoundException("Not found order to total price");
        BigDecimal total = BigDecimal.valueOf(0);
        for (OrderLineItems orderLineItemsDto: order.getOrderLineItemsList()) {
            BigDecimal tmp = productRepository.sumProductPriceByIdAndQuantity(orderLineItemsDto.getProductId(), orderLineItemsDto.getQuantity());
            total = total.add(tmp);
        }
        log.info("Total price for order -> " + orderId + " : " + total);
        return total;
    }

    @Override
    public List<ProductResponseDto> getProductByNameAndPrice(String name, BigDecimal price) {
        List<ProductEntity> productEntities = productRepository.findProductEntitiesByNameLikeAndPriceOrderByPrice(name, price);
        if (productEntities.isEmpty())
            throw new ProductNotFoundException("Not found any product like name and price");
        List<ProductResponseDto> responseDtos = productEntities.stream().map(this::mapToResponse).toList();
        log.info("List size getProductByNameAndPrice -> " + responseDtos);
        return responseDtos;
    }
}
