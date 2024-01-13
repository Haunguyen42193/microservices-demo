package com.example.orderservices.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName("orderResponse")
public class OrderResponse implements Serializable {
    private Long id;
    private List<OrderLineItemDto> orderLineItemDtos;
}
