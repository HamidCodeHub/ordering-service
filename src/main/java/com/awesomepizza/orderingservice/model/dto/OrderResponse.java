package com.awesomepizza.orderingservice.model.dto;

import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderCode;
    private OrderStatus status;
    private String statusDescription;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private String pizzaName;
        private Integer quantity;
        private String notes;
    }
}