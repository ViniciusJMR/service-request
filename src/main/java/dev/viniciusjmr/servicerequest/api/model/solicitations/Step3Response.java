package dev.viniciusjmr.servicerequest.api.model.solicitations;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.util.Date;

public record Step3Response(
        Solicitation.Priority priority,

        @JsonFormat(pattern = "yyyy-MM-dd")
        Date preferredDate,

        Double estimatedValue,
        Boolean termsAccepted,
        Integer currentStep
) {

    public static Step3Response from(Solicitation solicitation) {
        return new Step3Response(
                solicitation.getPriority(),
                solicitation.getPreferredDate(),
                solicitation.getEstimatedValue(),
                solicitation.getTermsAccepted(),
                solicitation.getCurrentStep()
        );
    }
}
