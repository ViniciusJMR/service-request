package dev.viniciusjmr.servicerequest.solicitation;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.model.State;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.StateRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import dev.viniciusjmr.servicerequest.domain.service.SolicitationService;
import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;
import dev.viniciusjmr.servicerequest.domain.service.cep.SearchCep;
import dev.viniciusjmr.servicerequest.domain.service.validation.GeneralValidator;
import dev.viniciusjmr.servicerequest.domain.service.validation.ValidationGroups;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private GeneralValidator generalValidator;

    @Mock
    private SearchCep searchCep;

    @Mock
    private Validator validator;

    private SolicitationService solicitationService;

    private UUID userId;
    private UUID solicitationId;

    private User user;
    private Solicitation solicitation;

    @BeforeEach
    void setUp() {
        solicitationService = new SolicitationService(
                solicitationRepository,
                userRepository,
                stateRepository,
                generalValidator,
                searchCep,
                validator
        );

        userId = UUID.randomUUID();
        solicitationId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        solicitation = new Solicitation();
        solicitation.setId(solicitationId);
        solicitation.setClient(user);
        solicitation.setStatus(Solicitation.Status.DRAFT);
        solicitation.setCurrentStep(1);
    }

    @Nested
    @DisplayName("Create solicitation")
    class CreateSolicitation {

        @Test
        void shouldCreateBlankSolicitationSuccessfully() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.of(user));
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.createBlankSolicitation(userId);

            assertSame(user, result.getClient());
            assertEquals(Solicitation.Status.DRAFT, result.getStatus());
            assertEquals(1, result.getCurrentStep());
        }

        @Test
        void shouldThrowResourceNotFoundWhenUserDoesNotExist() {
            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> solicitationService.createBlankSolicitation(userId)
            );

            verify(solicitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Step 1")
    class Step1 {

        @Test
        void shouldSaveStep1DraftSuccessfully() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep1(
                    userId,
                    solicitationId,
                    "  Instalacao eletrica  ",
                    "  Preciso realizar uma instalacao eletrica na residencia.  ",
                    Solicitation.ServiceType.INSTALLATION,
                    false
            );

            assertEquals("Instalacao eletrica", result.getTitle());
            assertEquals("Preciso realizar uma instalacao eletrica na residencia.", result.getDescription());
            assertEquals(Solicitation.ServiceType.INSTALLATION, result.getType());
            assertEquals(2, result.getCurrentStep());

            verify(generalValidator).validate(solicitation, userId, 1);
            verifyNoInteractions(validator);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldValidateWhenCompletingStep1() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep1.class))
                    .thenReturn(Set.of());
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep1(
                    userId,
                    solicitationId,
                    "Instalacao eletrica",
                    "Preciso realizar uma instalacao eletrica na residencia.",
                    Solicitation.ServiceType.INSTALLATION,
                    true
            );

            assertEquals(2, result.getCurrentStep());

            verify(generalValidator).validate(solicitation, userId, 1);
            verify(validator).validate(solicitation, ValidationGroups.OnCompleteStep1.class);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldNotSaveWhenStep1BeanValidationFails() {
            var titleViolation = violation("title", "Title must have between 3 and 80 characters");

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep1.class))
                    .thenReturn(Set.of(titleViolation));

            var exception = assertThrows(
                    FieldException.class,
                    () -> solicitationService.saveStep1(
                            userId,
                            solicitationId,
                            "",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION,
                            true
                    )
            );

            assertEquals("Invalid Fields", exception.getMessage());
            assertEquals("title", exception.getFields().getFirst().field());

            verify(solicitationRepository, never()).save(any());
        }

        @Test
        void shouldThrowResourceNotFoundWhenSolicitationDoesNotExist() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> solicitationService.saveStep1(
                            userId,
                            solicitationId,
                            null,
                            null,
                            null,
                            false
                    )
            );

            verifyNoInteractions(generalValidator, validator);
            verify(solicitationRepository, never()).save(any());
        }

        @Test
        void shouldNotSaveWhenGeneralValidationFails() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            doThrow(new RuntimeException("Invalid state"))
                    .when(generalValidator)
                    .validate(solicitation, userId, 1);

            assertThrows(
                    RuntimeException.class,
                    () -> solicitationService.saveStep1(
                            userId,
                            solicitationId,
                            "Title",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION,
                            false
                    )
            );

            verifyNoInteractions(validator);
            verify(solicitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Step 2")
    class Step2 {

        @Test
        void shouldSaveStep2DraftWithCepDataWhenViaCepReturnsAddress() {
            var state = state("SP", "Sao Paulo");
            var cepModel = cepModel("01001000", " Praca da Se ", " Se ", " Sao Paulo ", "SP");

            solicitation.setCurrentStep(2);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(searchCep.search("01001000"))
                    .thenReturn(Optional.of(cepModel));
            when(stateRepository.findByCode("SP"))
                    .thenReturn(Optional.of(state));
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep2(
                    userId,
                    solicitationId,
                    "01001-000",
                    " 12A ",
                    " apto 10 ",
                    false
            );

            var address = result.getAddress();

            assertEquals("01001000", address.getCep());
            assertEquals("12A", address.getNumber());
            assertEquals("apto 10", address.getComplement());
            assertEquals("Praca da Se", address.getStreet());
            assertEquals("Se", address.getNeighborhood());
            assertEquals("Sao Paulo", address.getCity());
            assertSame(state, address.getState());
            assertEquals(3, result.getCurrentStep());

            verify(generalValidator).validate(solicitation, userId, 2);
            verifyNoInteractions(validator);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldSaveStep2DraftWithOnlyRequestDataWhenViaCepReturnsEmpty() {
            solicitation.setCurrentStep(2);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(searchCep.search("01001000"))
                    .thenReturn(Optional.empty());
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep2(
                    userId,
                    solicitationId,
                    "01001-000",
                    "12A",
                    null,
                    false
            );

            var address = result.getAddress();

            assertEquals("01001000", address.getCep());
            assertEquals("12A", address.getNumber());
            assertEquals(3, result.getCurrentStep());

            verifyNoInteractions(stateRepository, validator);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldValidateWhenCompletingStep2() {
            var state = state("SP", "Sao Paulo");
            var cepModel = cepModel("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");

            solicitation.setCurrentStep(2);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(searchCep.search("01001000"))
                    .thenReturn(Optional.of(cepModel));
            when(stateRepository.findByCode("SP"))
                    .thenReturn(Optional.of(state));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep2.class))
                    .thenReturn(Set.of());
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep2(
                    userId,
                    solicitationId,
                    "01001-000",
                    "12A",
                    null,
                    true
            );

            assertEquals(3, result.getCurrentStep());
            assertSame(state, result.getAddress().getState());

            verify(generalValidator).validate(solicitation, userId, 2);
            verify(validator).validate(solicitation, ValidationGroups.OnCompleteStep2.class);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldNotSaveWhenStep2BeanValidationFails() {
            var cepModel = cepModel("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");
            var stateViolation = violation("address.state", "State is required");

            solicitation.setCurrentStep(2);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(searchCep.search("01001000"))
                    .thenReturn(Optional.of(cepModel));
            when(stateRepository.findByCode("SP"))
                    .thenReturn(Optional.empty());
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep2.class))
                    .thenReturn(Set.of(stateViolation));

            var exception = assertThrows(
                    FieldException.class,
                    () -> solicitationService.saveStep2(
                            userId,
                            solicitationId,
                            "01001-000",
                            "12A",
                            null,
                            true
                    )
            );

            assertEquals("address.state", exception.getFields().getFirst().field());

            verify(solicitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Step 3")
    class Step3 {

        @Test
        void shouldSaveStep3DraftSuccessfully() {
            var preferredDate = Date.from(Instant.now().plusSeconds(86400));

            solicitation.setCurrentStep(3);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep3(
                    userId,
                    solicitationId,
                    Solicitation.Priority.MEDIUM,
                    preferredDate,
                    250.0,
                    false,
                    false
            );

            assertEquals(Solicitation.Priority.MEDIUM, result.getPriority());
            assertEquals(preferredDate, result.getPreferredDate());
            assertEquals(250.0, result.getEstimatedValue());
            assertEquals(false, result.getTermsAccepted());

            verify(generalValidator).validate(solicitation, userId, 3);
            verifyNoInteractions(validator);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldValidateWhenCompletingStep3() {
            var preferredDate = Date.from(Instant.now().plusSeconds(86400));

            solicitation.setCurrentStep(3);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep3.class))
                    .thenReturn(Set.of());
            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep3(
                    userId,
                    solicitationId,
                    Solicitation.Priority.HIGH,
                    preferredDate,
                    150.0,
                    true,
                    true
            );

            assertEquals(Solicitation.Priority.HIGH, result.getPriority());
            assertEquals(true, result.getTermsAccepted());

            verify(generalValidator).validate(solicitation, userId, 3);
            verify(validator).validate(solicitation, ValidationGroups.OnCompleteStep3.class);
            verify(solicitationRepository).save(solicitation);
        }

        @Test
        void shouldNotSaveWhenStep3BeanValidationFails() {
            var estimatedValueViolation = violation("estimatedValue", "Estimated Value must be greater than 0");

            solicitation.setCurrentStep(3);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep3.class))
                    .thenReturn(Set.of(estimatedValueViolation));

            var exception = assertThrows(
                    FieldException.class,
                    () -> solicitationService.saveStep3(
                            userId,
                            solicitationId,
                            Solicitation.Priority.MEDIUM,
                            Date.from(Instant.now().plusSeconds(86400)),
                            -1.0,
                            true,
                            true
                    )
            );

            assertEquals("estimatedValue", exception.getFields().getFirst().field());

            verify(solicitationRepository, never()).save(any());
        }

        @Test
        void shouldNotSaveHighPriorityWithEstimatedValueLessThanOneHundred() {
            solicitation.setCurrentStep(3);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));
            when(validator.validate(solicitation, ValidationGroups.OnCompleteStep3.class))
                    .thenReturn(Set.of());

            var exception = assertThrows(
                    FieldException.class,
                    () -> solicitationService.saveStep3(
                            userId,
                            solicitationId,
                            Solicitation.Priority.HIGH,
                            Date.from(Instant.now().plusSeconds(86400)),
                            99.0,
                            true,
                            true
                    )
            );

            assertEquals("estimatedValue", exception.getFields().getFirst().field());

            verify(solicitationRepository, never()).save(any());
        }
    }

    private CEPModel cepModel(String cep, String street, String neighborhood, String city, String stateCode) {
        return new CEPModel(
                cep,
                street,
                null,
                neighborhood,
                city,
                stateCode
        );
    }

    private State state(String code, String name) {
        var state = new State();
        state.setCode(code);
        state.setName(name);
        return state;
    }

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Solicitation> violation(String propertyPath, String message) {
        ConstraintViolation<Solicitation> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);

        return violation;
    }
}
