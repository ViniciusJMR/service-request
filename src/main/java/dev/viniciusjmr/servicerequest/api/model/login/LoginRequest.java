package dev.viniciusjmr.servicerequest.api.model.login;

public record LoginRequest (
        String email,
        String password
) { }
