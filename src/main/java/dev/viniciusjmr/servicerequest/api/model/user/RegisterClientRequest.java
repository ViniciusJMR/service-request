package dev.viniciusjmr.servicerequest.api.model.user;

import jakarta.validation.constraints.Email;

public record RegisterClientRequest(
        String name,
        @Email(
                message = "Must be a valid email"
        )
        String email,
        String password
) { }
