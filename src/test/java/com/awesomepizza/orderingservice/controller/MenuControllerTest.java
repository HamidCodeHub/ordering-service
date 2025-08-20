package com.awesomepizza.orderingservice.controller;

import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;  // NEW IMPORT
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(MenuController.class)
@DisplayName("MenuController Integration Tests")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // CHANGED FROM @MockBean
    private PizzaRepository pizzaRepository;

    @Test
    @DisplayName("GET /api/v1/menu/pizzas - Should return available pizzas")
    void getAvailablePizzas_ShouldReturnList() throws Exception {
        // Arrange
        Pizza margherita = Pizza.builder()
                .id(1L)
                .name("Margherita")
                .description("Pomodoro, mozzarella, basilico")
                .price(new BigDecimal("8.00"))
                .available(true)
                .build();

        Pizza diavola = Pizza.builder()
                .id(2L)
                .name("Diavola")
                .description("Pomodoro, mozzarella, salame piccante")
                .price(new BigDecimal("10.00"))
                .available(true)
                .build();

        when(pizzaRepository.findByAvailableTrue())
                .thenReturn(Arrays.asList(margherita, diavola));

        // Act & Assert
        mockMvc.perform(get("/api/v1/menu/pizzas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Margherita"))
                .andExpect(jsonPath("$[0].price").value(8.00))
                .andExpect(jsonPath("$[1].name").value("Diavola"))
                .andExpect(jsonPath("$[1].price").value(10.00));
    }

    @Test
    @DisplayName("GET /api/v1/menu/pizzas - Should return empty list when no pizzas available")
    void getAvailablePizzas_WhenNoneAvailable_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(pizzaRepository.findByAvailableTrue())
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/menu/pizzas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}