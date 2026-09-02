package dev.viniciusjmr.servicerequest.domain.service.validation;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Step1Validator {

    public void validate (Solicitation solicitation) {
        List<FieldException.Field> errors = new ArrayList<>();

        if (solicitation.getType() == null) {
            errors.add(new FieldException.Field("serviceType", "Service Type is required"));
        }

        if (solicitation.getTitle() == null
                || solicitation.getTitle().length() < 3
                || solicitation.getTitle().length() > 80) {
            errors.add(new FieldException.Field("title", "Title must have between 3 and 80 characters"));
        }

        if (solicitation.getDescription() == null
                || solicitation.getDescription().length() < 20
                || solicitation.getDescription().length() > 1000) {
            errors.add(new FieldException.Field("description", "Description must have between 20 and 1000 characters"));
        }

        if (!errors.isEmpty()) {
            throw new FieldException("Invalid Operation", errors);
        }
    }
}
