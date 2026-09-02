package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.util.UUID;

public record Step1Response(
    UUID id,
    String title,
    String description,
    Solicitation.ServiceType serviceType
) {

    public static Step1Response from (Solicitation solicitation) {
        return new Step1Response(
                solicitation.getId(),
                solicitation.getTitle(),
                solicitation.getDescription(),
                solicitation.getType()
        );
    }
}
