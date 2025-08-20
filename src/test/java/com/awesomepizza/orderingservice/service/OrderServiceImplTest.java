package com.awesomepizza.orderingservice.service;
import com.awesomepizza.orderingservice.model.dto.CreateOrderRequest;
import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.dto.OrderStatusResponse;
import com.awesomepizza.orderingservice.model.dto.PizzaItemDto;
import com.awesomepizza.orderingservice.model.entity.Order;
import com.awesomepizza.orderingservice.model.entity.OrderItem;
import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.repository.OrderRepository;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PizzaRepository pizzaRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Pizza margherita;
    private Pizza diavola;
    private CreateOrderRequest validRequest;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // Setup test data
        margherita = Pizza.builder()
                .id(1L)
                .name("Margherita")
                .description("Pomodoro, mozzarella, basilico")
                .price(new BigDecimal("8.00"))
                .available(true)
                .build();

        diavola = Pizza.builder()
                .id(2L)
                .name("Diavola")
                .description("Pomodoro, mozzarella, salame piccante")
                .price(new BigDecimal("10.00"))
                .available(true)
                .build();

        // Create order request
        PizzaItemDto item1 = PizzaItemDto.builder()
                .pizzaId(1L)
                .quantity(2)
                .notes("Extra cheese")
                .build();

        PizzaItemDto item2 = PizzaItemDto.builder()
                .pizzaId(2L)
                .quantity(1)
                .notes(null)
                .build();

        validRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(item1, item2))
                .build();

        // Setup saved order
        savedOrder = Order.builder()
                .id(1L)
                .orderCode("ABC12345")
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create order successfully with multiple items")
    void createOrder_WithValidRequest_ShouldReturnOrderResponse() {
        // Arrange
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));
        when(pizzaRepository.findById(2L)).thenReturn(Optional.of(diavola));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ABC12345", response.getOrderCode());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals("In attesa", response.getStatusDescription());

        // Verify interactions
        verify(pizzaRepository, times(1)).findById(1L);
        verify(pizzaRepository, times(1)).findById(2L);
        verify(orderRepository, times(1)).save(any(Order.class));

        // Verify the order saved with correct details
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when pizza not found")
    void createOrder_WithInvalidPizzaId_ShouldThrowException() {
        // Arrange
        when(pizzaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(validRequest)
        );

        assertEquals("Pizza not found: 1", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with single item")
    void createOrder_WithSingleItem_ShouldReturnOrderResponse() {
        // Arrange
        CreateOrderRequest singleItemRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(
                        PizzaItemDto.builder()
                                .pizzaId(1L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(margherita));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.createOrder(singleItemRequest);

        // Assert
        assertNotNull(response);
        assertEquals("ABC12345", response.getOrderCode());
        verify(pizzaRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get order status successfully")
    void getOrderStatus_WithValidOrderCode_ShouldReturnStatus() {
        // Arrange
        Order orderInPreparation = Order.builder()
                .orderCode("ABC12345")
                .status(OrderStatus.IN_PREPARATION)
                .build();

        when(orderRepository.findByOrderCode("ABC12345"))
                .thenReturn(Optional.of(orderInPreparation));

        // Act
        OrderStatusResponse response = orderService.getOrderStatus("ABC12345");

        // Assert
        assertNotNull(response);
        assertEquals("ABC12345", response.getOrderCode());
        assertEquals(OrderStatus.IN_PREPARATION, response.getStatus());
        assertEquals("In preparazione", response.getStatusDescription());
        assertEquals("Il pizzaiolo sta preparando il tuo ordine", response.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    void getOrderStatus_WithInvalidOrderCode_ShouldThrowException() {
        // Arrange
        when(orderRepository.findByOrderCode("INVALID"))
                .thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderStatus("INVALID")
        );

        assertEquals("Order not found: INVALID", exception.getMessage());
    }

    @Test
    @DisplayName("Should return correct message for each order status")
    void getOrderStatus_ShouldReturnCorrectMessageForEachStatus() {
        // Test PENDING status
        testStatusMessage(OrderStatus.PENDING, "Il tuo ordine è in coda e verrà preso in carico a breve");

        // Test IN_PREPARATION status
        testStatusMessage(OrderStatus.IN_PREPARATION, "Il pizzaiolo sta preparando il tuo ordine");

        // Test READY status
        testStatusMessage(OrderStatus.READY, "Il tuo ordine è pronto!");

        // Test COMPLETED status
        testStatusMessage(OrderStatus.COMPLETED, "Ordine completato. Grazie!");
    }

    private void testStatusMessage(OrderStatus status, String expectedMessage) {
        Order order = Order.builder()
                .orderCode("TEST123")
                .status(status)
                .build();

        when(orderRepository.findByOrderCode("TEST123"))
                .thenReturn(Optional.of(order));

        OrderStatusResponse response = orderService.getOrderStatus("TEST123");
        assertEquals(expectedMessage, response.getMessage());
    }
}