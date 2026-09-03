package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model.SolicitationDocument;

import java.time.Instant;
import java.util.UUID;

public record SolicitationSearchResponse(
        UUID id,
        UUID clientId,
        Solicitation.Status status,
        Solicitation.ServiceType serviceType,
        String title,
        String description,
        String state,
        String city,
        Solicitation.Priority priority,
        Instant createdAt,
        Instant submittedAt,
        Instant updatedAt
) {

    public static SolicitationSearchResponse from(SolicitationDocument document) {
        return new SolicitationSearchResponse(
                document.getId(),
                document.getClientId(),
                document.getStatus(),
                document.getServiceType(),
                document.getTitle(),
                document.getDescription(),
                document.getState(),
                document.getCity(),
                document.getPriority(),
                document.getCreatedAt(),
                document.getSubmittedAt(),
                document.getUpdatedAt()
        );
    }
}
