package dev.viniciusjmr.servicerequest.api.model.user;

import dev.viniciusjmr.servicerequest.domain.model.User;

import java.util.UUID;

public record SecureUserResponse(
        UUID id,
        String name,
        String email
) {

    public static SecureUserResponse from(User user){
        return new SecureUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
