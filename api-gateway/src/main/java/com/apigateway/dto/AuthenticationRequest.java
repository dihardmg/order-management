package com.apigateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Authentication request for user login")
public class AuthenticationRequest {

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
    private String password;
}
