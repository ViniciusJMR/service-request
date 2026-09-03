package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.StateRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;
import dev.viniciusjmr.servicerequest.domain.service.cep.SearchCep;
import dev.viniciusjmr.servicerequest.domain.service.validation.GeneralValidator;
import dev.viniciusjmr.servicerequest.domain.service.validation.Step1Validator;
import dev.viniciusjmr.servicerequest.domain.service.validation.Step2Validator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SolicitationService {

    private final SolicitationRepository solicitationRepository;
    private final UserRepository userRepository;
    private final StateRepository stateRepository;
    private final GeneralValidator generalValidator;
    private final Step1Validator step1Validator;
    private final Step2Validator step2Validator;
    private final SearchCep searchCep;


    public SolicitationService(SolicitationRepository solicitationRepository, UserRepository userRepository, StateRepository stateRepository, GeneralValidator generalValidator, Step1Validator step1Validator, Step2Validator step2Validator, SearchCep searchCep) {
        this.solicitationRepository = solicitationRepository;
        this.userRepository = userRepository;
        this.stateRepository = stateRepository;
        this.generalValidator = generalValidator;
        this.step1Validator = step1Validator;
        this.step2Validator = step2Validator;
        this.searchCep = searchCep;
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
            Solicitation.ServiceType type
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
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 1);

        if (title != null)
            solicitation.setTitle(title.trim());

        if (description != null)
            solicitation.setDescription(description.trim());

        if (type != null)
            solicitation.setType(type);

        if (solicitation.getCurrentStep() == null || solicitation.getCurrentStep() < 2) {
            solicitation.setCurrentStep(2);
        }

        solicitation.setType(type);

        step1Validator.validate(solicitation);


        return solicitationRepository.save(solicitation);
    }

    public Solicitation saveStep2(
            UUID userId,
            UUID solicitationId,
            String cep,
            String number,
            String complement
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 2);

        var normalizedCep = SearchCep.normalizeCep(cep);

        var cepModel = searchCep.search(normalizedCep).orElse(new CEPModel());
        var address = buildAddress(cepModel, normalizedCep, number, complement);

        solicitation.setAddress(address);

        return solicitationRepository.save(solicitation);
    }

    public Solicitation completeStep2(
            UUID userId,
            UUID solicitationId,
            String cep,
            String number,
            String complement
    ) {
        var solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));

        generalValidator.validate(solicitation, userId, 2);

        var normalizedCep = SearchCep.normalizeCep(cep);
        var cepModel = searchCep.search(normalizedCep).orElse(new CEPModel());

        solicitation.setAddress(buildAddress(cepModel, normalizedCep, number, complement));
        step2Validator.validate(solicitation);

        solicitation.setCurrentStep(3);

        return solicitationRepository.save(solicitation);
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


    private void validateIfSolicitationIsFromClient(Solicitation solicitation, UUID userId) {
    }

    private void validateIfSolicitationCanBeEdited(Solicitation solicitation) {
    }

}
