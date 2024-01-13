package com.example.productservices.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    private String name;
    private String descriptions;
    private BigDecimal price;
    private int quantity;
}
