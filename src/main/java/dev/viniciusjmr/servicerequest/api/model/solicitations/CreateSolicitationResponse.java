package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.time.Instant;
import java.util.UUID;

public record CreateSolicitationResponse(
        UUID id,
        Solicitation.Status status,
        Instant createdAt
) {
    public static CreateSolicitationResponse from(Solicitation solicitation) {
        return new CreateSolicitationResponse(
                solicitation.getId(),
                solicitation.getStatus(),
                solicitation.getCreatedAt()
        );
    }
}
