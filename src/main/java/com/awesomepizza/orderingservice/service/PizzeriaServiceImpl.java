package com.awesomepizza.orderingservice.service;
import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.entity.Order;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import com.awesomepizza.orderingservice.exception.InvalidOrderStateException;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PizzeriaServiceImpl implements PizzeriaService {

    private final OrderRepository orderRepository;

    @Override
    public List<OrderResponse> getOrderQueue() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY
        );

        List<Order> orders = orderRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse takeNextOrder() {
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);

        if (pendingOrders.isEmpty()) {
            throw new OrderNotFoundException("No pending orders in queue");
        }

        Order order = pendingOrders.get(0);
        updateOrderStatus(order, OrderStatus.IN_PREPARATION);
        order.setStartedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} taken for preparation", savedOrder.getOrderCode());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse markOrderAsReady(String orderCode) {
        Order order = findOrderByCode(orderCode);
        updateOrderStatus(order, OrderStatus.READY);

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} marked as ready", savedOrder.getOrderCode());

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(String orderCode) {
        Order order = findOrderByCode(orderCode);
        updateOrderStatus(order, OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} completed", savedOrder.getOrderCode());

        return mapToOrderResponse(savedOrder);
    }

    private Order findOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderCode));
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus) {
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                    String.format("Cannot transition from %s to %s",
                            order.getStatus(), newStatus)
            );
        }
        order.setStatus(newStatus);
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
}