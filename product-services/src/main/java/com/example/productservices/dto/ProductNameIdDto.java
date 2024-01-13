package com.example.productservices.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductNameIdDto {
    private BigDecimal price;
    private String name;
}
