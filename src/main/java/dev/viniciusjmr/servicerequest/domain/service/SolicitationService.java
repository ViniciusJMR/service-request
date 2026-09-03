package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.StateRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;
import dev.viniciusjmr.servicerequest.domain.service.cep.SearchCep;
import dev.viniciusjmr.servicerequest.domain.service.validation.GeneralValidator;
import dev.viniciusjmr.servicerequest.domain.service.validation.ValidationGroups;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class SolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final GeneralValidator generalValidator;
    private final SearchCep searchCep;

    private final Validator validator;


    public SolicitationService(SolicitationRepository solicitationRepository, UserRepository userRepository, StateRepository stateRepository, GeneralValidator generalValidator, SearchCep searchCep, Validator validator) {
        this.solicitationRepository = solicitationRepository;
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.generalValidator = generalValidator;
        this.searchCep = searchCep;
        this.validator = validator;
    }

    public Solicitation createBlankSolicitation(UUID userId) {
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
            Solicitation.ServiceType type,
            boolean validate
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 1);

        if (title != null)
            solicitation.setTitle(title.trim());

        if (description != null)
            solicitation.setDescription(description.trim());

        if (type != null)
            solicitation.setType(type);

        solicitation.setCurrentStep(
                Math.max(solicitation.getCurrentStep(), 2)
        );

        solicitation.setType(type);

        if (validate) {
            validate(solicitation, ValidationGroups.OnCompleteStep1.class);
        }

        return solicitationRepository.save(solicitation);
    }

    public Solicitation saveStep2(
            UUID userId,
            UUID solicitationId,
            String cep,
            String number,
            String complement,
            boolean validate
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 2);

        var normalizedCep = SearchCep.normalizeCep(cep);
        var cepModel = searchCep.search(normalizedCep).orElse(new CEPModel());

        solicitation.setAddress(buildAddress(cepModel, normalizedCep, number, complement));

        if (validate){
            validate(solicitation, ValidationGroups.OnCompleteStep2.class);
        }

        solicitation.setCurrentStep(3);

        return solicitationRepository.save(solicitation);
    }

    public Solicitation saveStep3(
            UUID userId,
            UUID solicitationId,
            Solicitation.Priority priority,
            Date preferredDate,
            Double estimatedValue,
            Boolean termsAccepted,
            boolean validate
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 3);

        solicitation.setPriority(priority);
        solicitation.setPreferredDate(preferredDate);
        solicitation.setEstimatedValue(estimatedValue);
        solicitation.setTermsAccepted(termsAccepted);

        if (validate){
            validate(solicitation, ValidationGroups.OnCompleteStep3.class);

            if (solicitation.getPriority() == Solicitation.Priority.HIGH && solicitation.getEstimatedValue() < 100) {
                throw new FieldException(
                        "Invalid Fields",
                        List.of(
                                new FieldException.Field(
                                        "estimatedValue",
                                        "For High priorities estimated value can not be less then 100"
                                )
                        )
                );
            }
        }


        return solicitationRepository.save(solicitation);
    }

    public Solicitation submit(
            UUID userId,
            UUID solicitationId
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 3);

        validate(solicitation, ValidationGroups.OnSubmit.class);

        solicitation.setStatus(Solicitation.Status.SUBMITTED);
        solicitation.setSubmittedAt(Instant.now());

        return solicitationRepository.save(solicitation);
    }


    private void validate(Object object, Class<?> group) {
        var violations = validator.validate(object, group);

        if (!violations.isEmpty()) {
            var errors = violations.stream().map(m ->
                    new FieldException.Field(
                            m.getPropertyPath().toString(),
                            m.getMessage()
                    )
            ).toList();
            throw new FieldException("Invalid Fields", errors);
        }
    }

    private Address buildAddress(
            CEPModel cepModel,
            String normalizedCep,
            String number,
            String complement
    ) {
        var address = new Address();

        if (normalizedCep != null) {
            address.setCep(normalizedCep);
        }

        if (number != null) {
            address.setNumber(number.trim());
        }

        if (complement != null) {
            address.setComplement(complement.trim());
        }

        if (cepModel.getStreet() != null) {
            address.setStreet(cepModel.getStreet().trim());
        }

        if (cepModel.getNeighborhood() != null) {
            address.setNeighborhood(cepModel.getNeighborhood().trim());
        }

        if (cepModel.getCity() != null) {
            address.setCity(cepModel.getCity().trim());
        }

        if (cepModel.getState() != null) {
            stateRepository.findByCode(cepModel.getState())
                    .ifPresent(address::setState);
        }

        return address;
    }

}
