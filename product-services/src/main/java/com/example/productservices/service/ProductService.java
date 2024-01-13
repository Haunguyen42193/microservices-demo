package com.example.productservices.service;

import com.example.productservices.dto.OrderReceiveDto;
import com.example.productservices.dto.ProductNameIdDto;
import com.example.productservices.dto.ProductRequestDto;
import com.example.productservices.dto.ProductResponseDto;
import com.example.productservices.model.ProductEntity;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<ProductResponseDto> getAllProduct();

    ProductResponseDto getProductById(Long id);

    ProductResponseDto createProduct(ProductRequestDto productRequestDto);

    ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto);

    void deleteProduct(Long id);

    ProductResponseDto mapToResponse(ProductEntity productEntity);

    List<ProductResponseDto> getByNameId(ProductNameIdDto productNameIdDto);

    BigDecimal getTotalPrice(Long orderId);

    List<ProductResponseDto> getProductByNameAndPrice(String name, BigDecimal price);
}
