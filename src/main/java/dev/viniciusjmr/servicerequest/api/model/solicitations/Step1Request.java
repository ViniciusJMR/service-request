package dev.viniciusjmr.servicerequest.api.model.solicitations;

import jakarta.validation.constraints.Pattern;

public record Step1Request(
        String title,
        String description,

        @Pattern(
                regexp = "INSTALLATION|MAINTENANCE|INSPECTION",
                message = "Invalid service type. Valid types: INSTALLATION, MAINTENANCE, INSPECTION"
        )
        String serviceType
) {
}
