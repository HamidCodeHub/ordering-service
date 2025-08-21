package com.awesomepizza.orderingservice.repository;

import com.awesomepizza.orderingservice.model.entity.Order;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("OrderRepository Integration Tests")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    private Order pendingOrder1;
    private Order pendingOrder2;
    private Order inPreparationOrder;
    private Order completedOrder;

    @BeforeEach
    void setUp() {
        // Create test orders with different statuses
        pendingOrder1 = Order.builder()
                .status(OrderStatus.PENDING)
                .build();
        pendingOrder1.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        pendingOrder2 = Order.builder()
                .status(OrderStatus.PENDING)
                .build();
        pendingOrder2.setCreatedAt(LocalDateTime.now().minusMinutes(20));

        inPreparationOrder = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .build();
        inPreparationOrder.setCreatedAt(LocalDateTime.now().minusMinutes(40));

        completedOrder = Order.builder()
                .status(OrderStatus.COMPLETED)
                .build();
        completedOrder.setCreatedAt(LocalDateTime.now().minusMinutes(60));

        // Persist orders
        entityManager.persist(pendingOrder1);
        entityManager.persist(pendingOrder2);
        entityManager.persist(inPreparationOrder);
        entityManager.persist(completedOrder);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find order by order code")
    void findByOrderCode_ShouldReturnOrder() {
        // Act
        Optional<Order> found = orderRepository.findByOrderCode(pendingOrder1.getOrderCode());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(pendingOrder1.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Should return empty when order code not found")
    void findByOrderCode_WhenNotFound_ShouldReturnEmpty() {
        // Act
        Optional<Order> found = orderRepository.findByOrderCode("NONEXISTENT");

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should find orders by status ordered by creation time")
    void findByStatusOrderByCreatedAtAsc_ShouldReturnOrderedList() {
        // Act
        List<Order> pendingOrders = orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);

        // Assert
        assertEquals(2, pendingOrders.size());
        // First order should be the older one
        assertEquals(pendingOrder1.getId(), pendingOrders.get(0).getId());
        assertEquals(pendingOrder2.getId(), pendingOrders.get(1).getId());
    }

    @Test
    @DisplayName("Should find orders by multiple statuses")
    void findByStatusInOrderByCreatedAtAsc_ShouldReturnFilteredOrders() {
        // Act
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.IN_PREPARATION
        );
        List<Order> activeOrders = orderRepository.findByStatusInOrderByCreatedAtAsc(activeStatuses);

        // Assert
        assertEquals(3, activeOrders.size());
        // Should be ordered by creation time
        assertEquals(inPreparationOrder.getId(), activeOrders.get(2).getId()); // oldest
        assertEquals(pendingOrder1.getId(), activeOrders.get(0).getId());
        assertEquals(pendingOrder2.getId(), activeOrders.get(1).getId()); // newest
    }

    @Test
    @DisplayName("Should generate unique order code on persist")
    void save_ShouldGenerateUniqueOrderCode() {
        // Arrange
        Order newOrder = Order.builder()
                .status(OrderStatus.PENDING)
                .build();

        // Act
        Order savedOrder = orderRepository.save(newOrder);

        // Assert
        assertNotNull(savedOrder.getOrderCode());
        assertEquals(8, savedOrder.getOrderCode().length());
        assertTrue(savedOrder.getOrderCode().matches("[A-Z0-9]+"));
    }
}