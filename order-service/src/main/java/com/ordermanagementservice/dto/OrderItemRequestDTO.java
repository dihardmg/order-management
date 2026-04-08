package com.ordermanagementservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for order item in request
 * Represents items within an order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item request DTO")
public class OrderItemRequestDTO {

    @Schema(
        description = "Product ID to order",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(
        description = "Quantity of the product",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1"
    )
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
