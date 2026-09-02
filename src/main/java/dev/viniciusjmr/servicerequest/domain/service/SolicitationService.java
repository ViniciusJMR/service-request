package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.ForbidenOperationException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.exception.SolicitationNotEditableException;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import dev.viniciusjmr.servicerequest.domain.service.validation.Step1Validator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final UserRepository userRepository;
    private final Step1Validator step1Validator;


    public SolicitationService(SolicitationRepository solicitationRepository, UserRepository userRepository, Step1Validator step1Validator) {
        this.solicitationRepository = solicitationRepository;
        this.userRepository = userRepository;
        this.step1Validator = step1Validator;
    }

    public Solicitation createBlankRequest(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var solicitation = new Solicitation();

        solicitation.setClient(user);
        solicitation.setCurrentStep(1);
        solicitation.setStatus(Solicitation.Status.DRAFT);

        return solicitationRepository.save(solicitation);
    }

    public Solicitation saveStep1(
            UUID userId,
            UUID solicitationId,
            String title,
            String description,
            Solicitation.ServiceType type
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        validateIfSolicitationIsFromClient(solicitation, userId);
        validateIfSolicitationCanBeEdited(solicitation);

        if (title != null)
            solicitation.setTitle(title.trim());

        if (description != null)
            solicitation.setDescription(description.trim());

        if (type != null)
            solicitation.setType(type);

        return solicitationRepository.save(solicitation);
    }

    public Solicitation completeStep1(
            UUID userId,
            UUID solicitationId,
            String title,
            String description,
            Solicitation.ServiceType type
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        validateIfSolicitationIsFromClient(solicitation, userId);
        validateIfSolicitationCanBeEdited(solicitation);

        solicitation.setTitle(title.trim());
        solicitation.setDescription(description.trim());
        solicitation.setType(type);

        step1Validator.validate(solicitation);


        return solicitationRepository.save(solicitation);
    }

    private void validateIfSolicitationIsFromClient(Solicitation solicitation, UUID userId) {
        if (!solicitation.getClient().getId().equals(userId)) {
            throw new ForbidenOperationException("You do not have permission to modify this solicitation");
        }
    }

    private void validateIfSolicitationCanBeEdited(Solicitation solicitation) {
        if (!solicitation.getStatus().equals(Solicitation.Status.DRAFT)) {
            throw new SolicitationNotEditableException("Solicitation can only be edited in Draft");
        }
    }

}
