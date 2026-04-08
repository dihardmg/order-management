package com.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating an existing product
 * Used in PUT /api/products/{id} endpoint
 * All fields are optional for partial updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateDTO {

    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have maximum 10 integer digits and 2 decimal digits")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 1000000, message = "Stock cannot exceed 1,000,000")
    private Integer stock;

    private Long categoryId;

    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    private String sku;

    private Boolean isActive;
}