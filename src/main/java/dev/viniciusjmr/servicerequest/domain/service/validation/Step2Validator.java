package dev.viniciusjmr.servicerequest.domain.service.validation;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Step2Validator {

    public void validate(Solicitation solicitation) {
        List<FieldException.Field> errors = new ArrayList<>();
        Address address = solicitation.getAddress();

        if (address == null) {
            errors.add(new FieldException.Field("address", "Address is required"));
            throw new FieldException("Invalid Operation", errors);
        }

        if (isBlank(address.getCep()) || !address.getCep().matches("\\d{8}")) {
            errors.add(new FieldException.Field("cep", "CEP must have 8 digits"));
        }

        if (isBlank(address.getNumber())
                || address.getNumber().length() < 1
                || address.getNumber().length() > 20) {
            errors.add(new FieldException.Field("number", "Number must have between 1 and 20 characters"));
        }

        if (isBlank(address.getStreet())) {
            errors.add(new FieldException.Field("street", "Street is required"));
        }

        if (isBlank(address.getNeighborhood())) {
            errors.add(new FieldException.Field("neighborhood", "Neighborhood is required"));
        }

        if (isBlank(address.getCity())) {
            errors.add(new FieldException.Field("city", "City is required"));
        }

        if (address.getState() == null || isBlank(address.getState().getCode())) {
            errors.add(new FieldException.Field("state", "State is required"));
        } else if (!address.getState().getCode().matches("[A-Z]{2}")) {
            errors.add(new FieldException.Field("state", "State must be a valid UF"));
        }

        if (!errors.isEmpty()) {
            throw new FieldException("Invalid Operation", errors);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
