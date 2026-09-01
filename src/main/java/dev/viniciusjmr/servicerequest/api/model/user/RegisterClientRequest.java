package dev.viniciusjmr.servicerequest.api.model.user;

import dev.viniciusjmr.servicerequest.domain.model.User;

public record RegisterClientRequest(
        String name,
        String email,
        String password
) { }
