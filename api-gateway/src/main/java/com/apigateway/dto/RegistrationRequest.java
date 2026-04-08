package com.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Registration request for new user account")
public class RegistrationRequest {

    @Schema(
        description = "Username for the account",
        example = "fany",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 50
    )
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Schema(
        description = "User email address",
        example = "fany@email.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(
        description = "User password",
        example = "password",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 6,
        maxLength = 100
    )
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(
        description = "User full name",
        example = "Fany",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Full name is required")
    private String fullName;
}
