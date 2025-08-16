package com.awesomepizza.orderingservice.model.dto;

import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusResponse {
    private String orderCode;
    private OrderStatus status;
    private String statusDescription;
    private String message;
}
