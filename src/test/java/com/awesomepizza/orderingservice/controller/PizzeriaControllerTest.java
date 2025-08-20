package com.awesomepizza.orderingservice.controller;

import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.enums.OrderStatus;
import com.awesomepizza.orderingservice.exception.InvalidOrderStateException;
import com.awesomepizza.orderingservice.exception.OrderNotFoundException;
import com.awesomepizza.orderingservice.service.PizzeriaService;
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
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(PizzeriaController.class)
@DisplayName("PizzeriaController Integration Tests")
class PizzeriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // CHANGED FROM @MockBean
    private PizzeriaService pizzeriaService;

    private OrderResponse pendingOrder;
    private OrderResponse inPreparationOrder;

    @BeforeEach
    void setUp() {
        pendingOrder = OrderResponse.builder()
                .id(1L)
                .orderCode("ORDER001")
                .status(OrderStatus.PENDING)
                .statusDescription("In attesa")
                .createdAt(LocalDateTime.now())
                .build();

        inPreparationOrder = OrderResponse.builder()
                .id(2L)
                .orderCode("ORDER002")
                .status(OrderStatus.IN_PREPARATION)
                .statusDescription("In preparazione")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .startedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/pizzeria/queue - Should return order queue")
    void getOrderQueue_ShouldReturnAllActiveOrders() throws Exception {
        // Arrange
        when(pizzeriaService.getOrderQueue())
                .thenReturn(Arrays.asList(pendingOrder, inPreparationOrder));

        // Act & Assert
        mockMvc.perform(get("/api/v1/pizzeria/queue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderCode").value("ORDER001"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].orderCode").value("ORDER002"))
                .andExpect(jsonPath("$[1].status").value("IN_PREPARATION"));
    }

    @Test
    @DisplayName("GET /api/v1/pizzeria/queue - Should return empty list when no orders")
    void getOrderQueue_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(pizzeriaService.getOrderQueue())
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/pizzeria/queue")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /api/v1/pizzeria/orders/next - Should take next order")
    void takeNextOrder_ShouldReturnOrder() throws Exception {
        // Arrange
        when(pizzeriaService.takeNextOrder())
                .thenReturn(inPreparationOrder);

        // Act & Assert
        mockMvc.perform(post("/api/v1/pizzeria/orders/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("ORDER002"))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));
    }

    @Test
    @DisplayName("POST /api/v1/pizzeria/orders/next - Should return 404 when no pending orders")
    void takeNextOrder_WhenNoPendingOrders_ShouldReturn404() throws Exception {
        // Arrange
        when(pizzeriaService.takeNextOrder())
                .thenThrow(new OrderNotFoundException("No pending orders in queue"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/pizzeria/orders/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/pizzeria/orders/{orderCode}/ready - Should mark order as ready")
    void markOrderAsReady_ShouldUpdateStatus() throws Exception {
        // Arrange
        OrderResponse readyOrder = OrderResponse.builder()
                .id(2L)
                .orderCode("ORDER002")
                .status(OrderStatus.READY)
                .statusDescription("Pronto")
                .build();

        when(pizzeriaService.markOrderAsReady("ORDER002"))
                .thenReturn(readyOrder);

        // Act & Assert
        mockMvc.perform(put("/api/v1/pizzeria/orders/ORDER002/ready")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("ORDER002"))
                .andExpect(jsonPath("$.status").value("READY"));
    }

    @Test
    @DisplayName("PUT /api/v1/pizzeria/orders/{orderCode}/ready - Should return 400 for invalid transition")
    void markOrderAsReady_WithInvalidTransition_ShouldReturn400() throws Exception {
        // Arrange
        when(pizzeriaService.markOrderAsReady("ORDER001"))
                .thenThrow(new InvalidOrderStateException("Cannot transition from PENDING to READY"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/pizzeria/orders/ORDER001/ready")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/pizzeria/orders/{orderCode}/complete - Should complete order")
    void completeOrder_ShouldUpdateStatus() throws Exception {
        // Arrange
        OrderResponse completedOrder = OrderResponse.builder()
                .id(3L)
                .orderCode("ORDER003")
                .status(OrderStatus.COMPLETED)
                .statusDescription("Completato")
                .completedAt(LocalDateTime.now())
                .build();

        when(pizzeriaService.completeOrder("ORDER003"))
                .thenReturn(completedOrder);

        // Act & Assert
        mockMvc.perform(put("/api/v1/pizzeria/orders/ORDER003/complete")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value("ORDER003"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}