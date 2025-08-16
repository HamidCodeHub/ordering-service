package com.awesomepizza.orderingservice.service;

import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import java.util.List;

public interface PizzeriaService {
    List<OrderResponse> getOrderQueue();
    OrderResponse takeNextOrder();
    OrderResponse markOrderAsReady(String orderCode);
    OrderResponse completeOrder(String orderCode);
}