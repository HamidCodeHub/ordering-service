package com.awesomepizza.orderingservice.repository;
import com.awesomepizza.orderingservice.model.entity.Order;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);
    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);
}