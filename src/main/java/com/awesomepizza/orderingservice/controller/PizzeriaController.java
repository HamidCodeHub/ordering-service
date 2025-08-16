package com.awesomepizza.orderingservice.controller;

import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.exception.GlobalExceptionHandler.ErrorResponse;
import com.awesomepizza.orderingservice.service.PizzeriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/pizzeria")
@RequiredArgsConstructor
@Tag(name = "Pizzeria Management", description = "Pizzeria staff operations")
public class PizzeriaController {

    private final PizzeriaService pizzeriaService;

    @Operation(
            summary = "View order queue",
            description = "Get all active orders in the queue (PENDING, IN_PREPARATION, READY)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Queue retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))
                    )
            )
    })
    @GetMapping("/queue")
    public ResponseEntity<List<OrderResponse>> getOrderQueue() {
        List<OrderResponse> queue = pizzeriaService.getOrderQueue();
        return ResponseEntity.ok(queue);
    }

    @Operation(
            summary = "Take next order",
            description = "Take the next pending order from the queue and start preparation"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order taken successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No pending orders in queue",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/orders/next")
    public ResponseEntity<OrderResponse> takeNextOrder() {
        OrderResponse response = pizzeriaService.takeNextOrder();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Mark order as ready",
            description = "Mark an order that is being prepared as ready for pickup"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order marked as ready",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order state transition",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/orders/{orderCode}/ready")
    public ResponseEntity<OrderResponse> markOrderAsReady(
            @Parameter(description = "Order code", example = "ABC12345")
            @PathVariable String orderCode) {
        OrderResponse response = pizzeriaService.markOrderAsReady(orderCode);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Complete order",
            description = "Mark an order as completed (delivered to customer)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order state transition",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/orders/{orderCode}/complete")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Order code", example = "ABC12345")
            @PathVariable String orderCode) {
        OrderResponse response = pizzeriaService.completeOrder(orderCode);
        return ResponseEntity.ok(response);
    }
}