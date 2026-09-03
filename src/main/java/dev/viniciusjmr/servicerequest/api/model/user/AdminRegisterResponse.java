package dev.viniciusjmr.servicerequest.api.model.user;

import dev.viniciusjmr.servicerequest.domain.model.AnalystCoverage;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.State;
import dev.viniciusjmr.servicerequest.domain.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record AdminRegisterResponse(
        UUID id,
        String name,
        String email,
        Role role,
        boolean enabled,
        Instant createdAt,
        Set<String> states

) {

    public static AdminRegisterResponse from(AnalystCoverage analystCoverage) {
        var user = analystCoverage.getUser();

        return new AdminRegisterResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                analystCoverage.getStates()
                        .stream()
                        .map(State::getCode)
                        .collect(Collectors.toSet())
        );
    }
}
