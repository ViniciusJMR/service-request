package dev.viniciusjmr.servicerequest.api.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record AdminRegisterUserRequest(
        String name,
        @Email(
                message = "Must be a valid email"
        )
        String email,
        String password,

        @NotBlank(
                message = "Role is required"
        )
        @Pattern(
                message = "Role must be ADMIN or ANALYST",
                regexp = "ADMIN|ANALYST"
        )
        String role,

        @Pattern(
                message = "States must be 2 Uppercase letter",
                regexp = "[A-Z]{2}"
        )
        Set<String> states
) {
}
