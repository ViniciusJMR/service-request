package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

public record Step1Request(
        String title,
        String description,
        Solicitation.ServiceType serviceType
) {
}
