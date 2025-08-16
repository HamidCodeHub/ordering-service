package com.awesomepizza.orderingservice.controller;


import com.awesomepizza.orderingservice.model.entity.Pizza;
import com.awesomepizza.orderingservice.repository.PizzaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Pizza menu operations")
public class MenuController {

    private final PizzaRepository pizzaRepository;

    @Operation(
            summary = "Get available pizzas",
            description = "Retrieve all available pizzas from the menu"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Pizza.class))
                    )
            )
    })
    @GetMapping("/pizzas")
    public ResponseEntity<List<Pizza>> getAvailablePizzas() {
        List<Pizza> pizzas = pizzaRepository.findByAvailableTrue();
        return ResponseEntity.ok(pizzas);
    }
}
