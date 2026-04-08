package com.ordermanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a new order
 * Used for REST API request validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new order")
public class OrderRequestDTO {

    @Schema(
        description = "Customer ID placing the order",
        example = "6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Schema(
        description = "List of items in the order",
        example = "[{\"productId\": 2, \"quantity\": 1}]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "At least one order item is required")
    @Valid
    private List<OrderItemRequestDTO> items;
}
