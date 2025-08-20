package com.awesomepizza.orderingservice.service;

import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.entity.Order;
import com.awesomepizza.orderingservice.model.entity.OrderItem;
import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import com.awesomepizza.orderingservice.exception.InvalidOrderStateException;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PizzeriaService Unit Tests")
class PizzeriaServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PizzeriaServiceImpl pizzeriaService;

    private Order pendingOrder;
    private Order inPreparationOrder;
    private Order readyOrder;

    @BeforeEach
    void setUp() {
        Pizza margherita = Pizza.builder()
                .id(1L)
                .name("Margherita")
                .price(new BigDecimal("8.00"))
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .pizza(margherita)
                .quantity(2)
                .build();

        pendingOrder = Order.builder()
                .id(1L)
                .orderCode("ORDER001")
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(item))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        inPreparationOrder = Order.builder()
                .id(2L)
                .orderCode("ORDER002")
                .status(OrderStatus.IN_PREPARATION)
                .items(Arrays.asList(item))
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        readyOrder = Order.builder()
                .id(3L)
                .orderCode("ORDER003")
                .status(OrderStatus.READY)
                .items(Arrays.asList(item))
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .startedAt(LocalDateTime.now().minusMinutes(15))
                .build();
    }

    @Test
    @DisplayName("Should get order queue with all active orders")
    void getOrderQueue_ShouldReturnAllActiveOrders() {
        // Arrange
        List<Order> activeOrders = Arrays.asList(pendingOrder, inPreparationOrder, readyOrder);
        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any(List.class)))
                .thenReturn(activeOrders);

        // Act
        List<OrderResponse> queue = pizzeriaService.getOrderQueue();

        // Assert
        assertNotNull(queue);
        assertEquals(3, queue.size());
        assertEquals("ORDER001", queue.get(0).getOrderCode());
        assertEquals("ORDER002", queue.get(1).getOrderCode());
        assertEquals("ORDER003", queue.get(2).getOrderCode());

        // Verify correct statuses were queried
        ArgumentCaptor<List<OrderStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).findByStatusInOrderByCreatedAtAsc(statusCaptor.capture());
        List<OrderStatus> queriedStatuses = statusCaptor.getValue();
        assertTrue(queriedStatuses.contains(OrderStatus.PENDING));
        assertTrue(queriedStatuses.contains(OrderStatus.IN_PREPARATION));
        assertTrue(queriedStatuses.contains(OrderStatus.READY));
        assertFalse(queriedStatuses.contains(OrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("Should take next pending order successfully")
    void takeNextOrder_WithPendingOrders_ShouldReturnFirstOrder() {
        // Arrange
        List<Order> pendingOrders = Arrays.asList(pendingOrder);
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING))
                .thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        // Act
        OrderResponse response = pizzeriaService.takeNextOrder();

        // Assert
        assertNotNull(response);
        assertEquals("ORDER001", response.getOrderCode());

        // Verify order status was updated
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.IN_PREPARATION, capturedOrder.getStatus());
        assertNotNull(capturedOrder.getStartedAt());
    }

    @Test
    @DisplayName("Should throw exception when no pending orders")
    void takeNextOrder_WithNoPendingOrders_ShouldThrowException() {
        // Arrange
        when(orderRepository.findByStatusOrderByCreatedAtAsc(OrderStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> pizzeriaService.takeNextOrder()
        );

        assertEquals("No pending orders in queue", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should mark order as ready successfully")
    void markOrderAsReady_WithValidTransition_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findByOrderCode("ORDER002"))
                .thenReturn(Optional.of(inPreparationOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(inPreparationOrder);

        // Act
        OrderResponse response = pizzeriaService.markOrderAsReady("ORDER002");

        // Assert
        assertNotNull(response);
        assertEquals("ORDER002", response.getOrderCode());

        // Verify status transition
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.READY, orderCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should throw exception for invalid status transition")
    void markOrderAsReady_WithInvalidTransition_ShouldThrowException() {
        // Arrange - trying to mark PENDING order as READY (invalid transition)
        when(orderRepository.findByOrderCode("ORDER001"))
                .thenReturn(Optional.of(pendingOrder));

        // Act & Assert
        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> pizzeriaService.markOrderAsReady("ORDER001")
        );

        assertEquals("Cannot transition from PENDING to READY", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should complete order successfully")
    void completeOrder_WithValidTransition_ShouldUpdateStatus() {
        // Arrange
        when(orderRepository.findByOrderCode("ORDER003"))
                .thenReturn(Optional.of(readyOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(readyOrder);

        // Act
        OrderResponse response = pizzeriaService.completeOrder("ORDER003");

        // Assert
        assertNotNull(response);
        assertEquals("ORDER003", response.getOrderCode());

        // Verify completion
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.COMPLETED, capturedOrder.getStatus());
        assertNotNull(capturedOrder.getCompletedAt());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void completeOrder_WithInvalidOrderCode_ShouldThrowException() {
        // Arrange
        when(orderRepository.findByOrderCode("INVALID"))
                .thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> pizzeriaService.completeOrder("INVALID")
        );

        assertEquals("Order not found: INVALID", exception.getMessage());
    }

    @Test
    @DisplayName("Should handle order with multiple items correctly")
    void getOrderQueue_WithMultipleItems_ShouldMapCorrectly() {
        // Arrange
        Pizza margherita = Pizza.builder().name("Margherita").build();
        Pizza diavola = Pizza.builder().name("Diavola").build();

        OrderItem item1 = OrderItem.builder()
                .pizza(margherita)
                .quantity(2)
                .notes("Extra cheese")
                .build();

        OrderItem item2 = OrderItem.builder()
                .pizza(diavola)
                .quantity(1)
                .notes(null)
                .build();

        Order orderWithMultipleItems = Order.builder()
                .id(4L)
                .orderCode("ORDER004")
                .status(OrderStatus.PENDING)
                .items(Arrays.asList(item1, item2))
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByStatusInOrderByCreatedAtAsc(any(List.class)))
                .thenReturn(Arrays.asList(orderWithMultipleItems));

        // Act
        List<OrderResponse> queue = pizzeriaService.getOrderQueue();

        // Assert
        assertEquals(1, queue.size());
        OrderResponse response = queue.get(0);
        assertEquals(2, response.getItems().size());
        assertEquals("Margherita", response.getItems().get(0).getPizzaName());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals("Extra cheese", response.getItems().get(0).getNotes());
        assertEquals("Diavola", response.getItems().get(1).getPizzaName());
        assertEquals(1, response.getItems().get(1).getQuantity());
    }
}
