package com.awesomepizza.orderingservice.service;

import com.awesomepizza.orderingservice.model.dto.CreateOrderRequest;
import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.dto.OrderStatusResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderStatusResponse getOrderStatus(String orderCode);
}
