package com.awesomepizza.orderingservice.service;

import com.awesomepizza.orderingservice.model.dto.*;
import com.awesomepizza.orderingservice.model.entity.*;
import com.awesomepizza.orderingservice.model.enums.*;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.repository.OrderRepository;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PizzaRepository pizzaRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order with {} items", request.getItems().size());

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .build();

        for (PizzaItemDto itemDto : request.getItems()) {
            Pizza pizza = pizzaRepository.findById(itemDto.getPizzaId())
                    .orElseThrow(() -> new IllegalArgumentException("Pizza not found: " + itemDto.getPizzaId()));

            OrderItem orderItem = OrderItem.builder()
                    .pizza(pizza)
                    .quantity(itemDto.getQuantity())
                    .notes(itemDto.getNotes())
                    .build();

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with code: {}", savedOrder.getOrderCode());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderStatusResponse getOrderStatus(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderCode));

        String message = generateStatusMessage(order.getStatus());

        return OrderStatusResponse.builder()
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .message(message)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusDescription(order.getStatus().getDescription())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemResponse.builder()
                                .pizzaName(item.getPizza().getName())
                                .quantity(item.getQuantity())
                                .notes(item.getNotes())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .startedAt(order.getStartedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

    private String generateStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Il tuo ordine è in coda e verrà preso in carico a breve";
            case IN_PREPARATION -> "Il pizzaiolo sta preparando il tuo ordine";
            case READY -> "Il tuo ordine è pronto!";
            case COMPLETED -> "Ordine completato. Grazie!";
        };
    }
}