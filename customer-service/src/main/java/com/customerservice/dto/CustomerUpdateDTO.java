package com.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing customer
 * Used in PUT /api/customers/{id} endpoint
 * All fields are optional for partial updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateDTO {

    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Phone must contain only numbers")
    private String phone;
}
