package dev.viniciusjmr.servicerequest.api.model.solicitations;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record Step3Request(
        @Pattern(
                message = "Priority must be LOW, MEDIUM or HIGH",
                regexp = "LOW|MEDIUM|HIGH"
        )
        String priority,
        @JsonFormat(pattern = "yyyy-MM-dd")
        Date preferredDate,
        Double estimatedValue,
        Boolean termsAccepted
) {
}
