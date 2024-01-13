package com.example.productservices.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto implements Serializable {
    private Long id;
    private String name;
    private String descriptions;
    private BigDecimal price;
    private int quantity;
}
