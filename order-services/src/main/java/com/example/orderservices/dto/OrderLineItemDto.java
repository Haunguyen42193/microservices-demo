package com.example.orderservices.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineItemDto implements Serializable {
    private Long productId;
    private int quantity;
}
