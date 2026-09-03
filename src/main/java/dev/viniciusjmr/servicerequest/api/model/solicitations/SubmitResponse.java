package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.util.Date;
import java.util.UUID;

public record SubmitResponse(
        UUID id,

        Solicitation.Status status,

        Integer currentStep,

// Step 1

        Solicitation.ServiceType type,

        String title,

        String description,


// Step 2
        Address address,

// Step 3
        Solicitation.Priority priority,

        Date preferredDate,

        Double estimatedValue,

        Boolean termsAccepted
) {

    public static SubmitResponse from(Solicitation solicitation) {
        return new SubmitResponse(
                solicitation.getId(),
                solicitation.getStatus(),
                solicitation.getCurrentStep(),
                solicitation.getType(),
                solicitation.getTitle(),
                solicitation.getDescription(),
                solicitation.getAddress(),
                solicitation.getPriority(),
                solicitation.getPreferredDate(),
                solicitation.getEstimatedValue(),
                solicitation.getTermsAccepted()
        );
    }
}
