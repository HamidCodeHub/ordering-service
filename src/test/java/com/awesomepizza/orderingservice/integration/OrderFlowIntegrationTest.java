package com.awesomepizza.orderingservice.integration;

import com.awesomepizza.orderingservice.model.dto.CreateOrderRequest;
import com.awesomepizza.orderingservice.model.dto.PizzaItemDto;
import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
@DisplayName("Order Flow Integration Tests")
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PizzaRepository pizzaRepository;

    private Pizza margherita;
    private Pizza diavola;

    @BeforeEach
    void setUp() {
        // Setup test pizzas
        margherita = pizzaRepository.save(Pizza.builder()
                .name("Margherita")
                .description("Pomodoro, mozzarella, basilico")
                .price(new BigDecimal("8.00"))
                .available(true)
                .build());

        diavola = pizzaRepository.save(Pizza.builder()
                .name("Diavola")
                .description("Pomodoro, mozzarella, salame piccante")
                .price(new BigDecimal("10.00"))
                .available(true)
                .build());
    }

    @Test
    @DisplayName("Should complete full order flow from creation to completion")
    void testCompleteOrderFlow() throws Exception {
        // Step 1: Create order
        PizzaItemDto item = PizzaItemDto.builder()
                .pizzaId(margherita.getId())
                .quantity(2)
                .notes("Extra cheese")
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(Arrays.asList(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String orderCode = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("orderCode").asText();

        // Step 2: Check order status
        mockMvc.perform(get("/api/v1/orders/{orderCode}/status", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Il tuo ordine è in coda e verrà preso in carico a breve"));

        // Step 3: Pizzeria takes the order
        mockMvc.perform(post("/api/v1/pizzeria/orders/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderCode").value(orderCode))
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // Step 4: Mark order as ready
        mockMvc.perform(put("/api/v1/pizzeria/orders/{orderCode}/ready", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));

        // Step 5: Check updated status
        mockMvc.perform(get("/api/v1/orders/{orderCode}/status", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.message").value("Il tuo ordine è pronto!"));

        // Step 6: Complete order
        mockMvc.perform(put("/api/v1/pizzeria/orders/{orderCode}/complete", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Step 7: Verify final status
        mockMvc.perform(get("/api/v1/orders/{orderCode}/status", orderCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Ordine completato. Grazie!"));
    }

    @Test
    @DisplayName("Should handle multiple orders in queue")
    void testMultipleOrdersInQueue() throws Exception {
        // Create first order
        CreateOrderRequest order1 = CreateOrderRequest.builder()
                .items(Arrays.asList(
                        PizzaItemDto.builder()
                                .pizzaId(margherita.getId())
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isCreated());

        // Create second order
        CreateOrderRequest order2 = CreateOrderRequest.builder()
                .items(Arrays.asList(
                        PizzaItemDto.builder()
                                .pizzaId(diavola.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isCreated());

        // Check queue has 2 orders
        mockMvc.perform(get("/api/v1/pizzeria/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        // Take first order
        mockMvc.perform(post("/api/v1/pizzeria/orders/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        // Check queue now has 1 pending and 1 in preparation
        mockMvc.perform(get("/api/v1/pizzeria/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("IN_PREPARATION"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));
    }
}