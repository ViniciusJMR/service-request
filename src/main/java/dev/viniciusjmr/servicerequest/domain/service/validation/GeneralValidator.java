package dev.viniciusjmr.servicerequest.domain.service.validation;

import dev.viniciusjmr.servicerequest.domain.exception.ForbidenOperationException;
import dev.viniciusjmr.servicerequest.domain.exception.NotEditableException;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GeneralValidator {

    public void validate(Solicitation solicitation, UUID userId, Integer currentStep) {
        if (!solicitation.getClient().getId().equals(userId)) {
            throw new ForbidenOperationException("You do not have permission to modify this solicitation");
        }

        if (!solicitation.getStatus().equals(Solicitation.Status.DRAFT)) {
            throw new NotEditableException("Solicitation can only be edited in Draft");
        }

        if(solicitation.getCurrentStep() < currentStep) {
            throw new NotEditableException(
                    String.format(
                            "Step %d can not be edited until you complete Step %d",
                            currentStep,
                            solicitation.getCurrentStep()
                    )
            );
        }
    }

}
