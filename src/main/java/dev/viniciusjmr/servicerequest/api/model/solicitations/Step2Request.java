package dev.viniciusjmr.servicerequest.api.model.solicitations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Step2Request(
        String cep,

        String number,

        String complement
) {
}
