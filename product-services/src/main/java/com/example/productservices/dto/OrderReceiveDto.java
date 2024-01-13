package com.example.productservices.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderReceiveDto {
    private Long id;
    private List<OrderLineItemsDto> orderLineItemsList;
}
