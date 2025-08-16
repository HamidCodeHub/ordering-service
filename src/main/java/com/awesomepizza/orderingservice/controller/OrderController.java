package com.awesomepizza.orderingservice.controller;
import com.awesomepizza.orderingservice.model.dto.CreateOrderRequest;
import com.awesomepizza.orderingservice.model.dto.OrderResponse;
import com.awesomepizza.orderingservice.model.dto.OrderStatusResponse;
import com.awesomepizza.orderingservice.exception.GlobalExceptionHandler.ErrorResponse;
import com.awesomepizza.orderingservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Customer order operations")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Place a new pizza order. No registration required. Returns an order code for tracking."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "id": 1,
                        "orderCode": "ABC12345",
                        "status": "PENDING",
                        "statusDescription": "In attesa",
                        "items": [
                            {
                                "pizzaName": "Margherita",
                                "quantity": 2,
                                "notes": "Extra cheese"
                            }
                        ],
                        "createdAt": "2024-01-15T10:30:00"
                    }
                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pizza not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Order details",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateOrderRequest.class),
                    examples = @ExampleObject(value = """
                {
                    "items": [
                        {
                            "pizzaId": 1,
                            "quantity": 2,
                            "notes": "Extra cheese"
                        },
                        {
                            "pizzaId": 3,
                            "quantity": 1,
                            "notes": null
                        }
                    ]
                }
                """)
            )
    )
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Check order status",
            description = "Get the current status of an order using the order code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderStatusResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "orderCode": "ABC12345",
                        "status": "IN_PREPARATION",
                        "statusDescription": "In preparazione",
                        "message": "Il pizzaiolo sta preparando il tuo ordine"
                    }
                    """)
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
    @GetMapping("/{orderCode}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @Parameter(description = "Order code received when order was created", example = "ABC12345")
            @PathVariable String orderCode) {
        OrderStatusResponse response = orderService.getOrderStatus(orderCode);
        return ResponseEntity.ok(response);
    }
}