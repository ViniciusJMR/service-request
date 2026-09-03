package dev.viniciusjmr.servicerequest.infrastructure.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;

public record ViaCepResponse(
        String cep,

        @JsonProperty("logradouro")
        String street,

        @JsonProperty("complemento")
        String complement,

        @JsonProperty("bairro")
        String neighborhood,

        @JsonProperty("localidade")
        String city,

        @JsonProperty("uf")
        String state,

        Boolean erro
) {

    public CEPModel toCEPModel() {
        return new CEPModel(
                cep,
                street,
                complement,
                neighborhood,
                city,
                state
        );
    }
}
