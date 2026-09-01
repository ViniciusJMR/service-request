package dev.viniciusjmr.servicerequest.api.model.user;

import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public record RegisterClientResponse(
        UUID id,
        String name,
        String email,
        Role role,
        Instant createdAt
) {

    public static RegisterClientResponse from(User user) {
        return new RegisterClientResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
