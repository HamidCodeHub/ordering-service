package com.awesomepizza.orderingservice.controller;

import com.awesomepizza.orderingservice.model.dto.CreateOrderRequest;
import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.dto.OrderStatusResponse;
import com.awesomepizza.orderingservice.model.dto.PizzaItemDto;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // NEW IMPORT
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Integration Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // CHANGED FROM @MockBean
    private OrderService orderService;

    private CreateOrderRequest validRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        PizzaItemDto item = PizzaItemDto.builder()
                .pizzaId(1L)
                .quantity(2)
                .notes("Extra cheese")
                .build();

        validRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(item))
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderCode("ABC12345")
                .status(OrderStatus.PENDING)
                .statusDescription("In attesa")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should create order successfully")
    void createOrder_WithValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderCode").value("ABC12345"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.statusDescription").value("In attesa"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 400 for empty items")
    void createOrder_WithEmptyItems_ShouldReturn400() throws Exception {
        // Arrange
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .items(Arrays.asList())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 400 for invalid quantity")
    void createOrder_WithInvalidQuantity_ShouldReturn400() throws Exception {
        // Arrange
        PizzaItemDto invalidItem = PizzaItemDto.builder()
                .pizzaId(1L)
                .quantity(0) // Invalid: minimum is 1
                .build();

        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(invalidItem))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderCode}/status - Should return order status")
    void getOrderStatus_WithValidCode_ShouldReturn200() throws Exception {
        // Arrange
        OrderStatusResponse statusResponse = OrderStatusResponse.builder()
                .orderCode("ABC12345")
                .status(OrderStatus.IN_PREPARATION)
                .statusDescription("In preparazione")
                .message("Il pizzaiolo sta preparando il tuo ordine")
                .build();

        when(orderService.getOrderStatus("ABC12345"))
                .thenReturn(statusResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/ABC12345/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("ABC12345"))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"))
                .andExpect(jsonPath("$.statusDescription").value("In preparazione"))
                .andExpect(jsonPath("$.message").value("Il pizzaiolo sta preparando il tuo ordine"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderCode}/status - Should return 404 for invalid code")
    void getOrderStatus_WithInvalidCode_ShouldReturn404() throws Exception {
        // Arrange
        when(orderService.getOrderStatus("INVALID"))
                .thenThrow(new OrderNotFoundException("Order not found: INVALID"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/INVALID/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should handle multiple items")
    void createOrder_WithMultipleItems_ShouldReturn201() throws Exception {
        // Arrange
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

        CreateOrderRequest multiItemRequest = CreateOrderRequest.builder()
                .items(Arrays.asList(item1, item2))
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(orderResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(multiItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderCode").value("ABC12345"));
    }
}