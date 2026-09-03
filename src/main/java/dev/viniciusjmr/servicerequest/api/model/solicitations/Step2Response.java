package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.util.UUID;

public record Step2Response(
        UUID id,
        Integer currentStep,
        String cep,
        String number,
        String complement,
        String street,
        String neighborhood,
        String city,
        String state
) {

    public static Step2Response from(Solicitation solicitation) {
        Address address = solicitation.getAddress();

        return new Step2Response(
                solicitation.getId(),
                solicitation.getCurrentStep(),
                address != null ? address.getCep() : null,
                address != null ? address.getNumber() : null,
                address != null ? address.getComplement() : null,
                address != null ? address.getStreet() : null,
                address != null ? address.getNeighborhood() : null,
                address != null ? address.getCity() : null,
                address != null && address.getState() != null ? address.getState().getCode() : null
        );
    }
}
