package dev.viniciusjmr.servicerequest.api.model.solicitations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SolicitationDecideRequest(

        @NotBlank( message = "Decision is required" )
        @Pattern(
                message = "Decision must be APPROVE or REJECT",
                regexp = "APPROVE|REJECT"
        )
        String decision,

        @NotBlank( message = "Comment is required")
        @Size(min = 10, max = 1000)
        String comment
) {
}
