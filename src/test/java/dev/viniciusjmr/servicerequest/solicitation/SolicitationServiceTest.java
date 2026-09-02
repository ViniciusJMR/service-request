package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.exception.ForbidenOperationException;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.exception.SolicitationNotEditableException;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import dev.viniciusjmr.servicerequest.domain.service.validation.Step1Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Step1Validator step1Validator;

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
                step1Validator
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
    @DisplayName("Step 1 Validation")
    class Step1 {
        @Test
        void shouldSaveStep1Successfully() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.saveStep1(
                    userId,
                    solicitationId,
                    "  Instalação elétrica  ",
                    "  Preciso realizar uma instalação elétrica na residência.  ",
                    Solicitation.ServiceType.INSTALLATION
            );

            ArgumentCaptor<Solicitation> captor =
                    ArgumentCaptor.forClass(Solicitation.class);

            verify(solicitationRepository).save(captor.capture());

            var savedSolicitation = captor.getValue();

            assertEquals(
                    "Instalação elétrica",
                    savedSolicitation.getTitle()
            );

            assertEquals(
                    "Preciso realizar uma instalação elétrica na residência.",
                    savedSolicitation.getDescription()
            );

            assertEquals(
                    Solicitation.ServiceType.INSTALLATION,
                    savedSolicitation.getType()
            );

            assertSame(savedSolicitation, result);

            verifyNoInteractions(step1Validator);
        }

        @Test
        void shouldThrowResourceNotFoundWhenSavingStep1AndSolicitationDoesNotExist() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> solicitationService.saveStep1(
                            userId,
                            solicitationId,
                            null,
                            null,
                            null
                    )
            );

            verify(solicitationRepository, never())
                    .save(any());

            verifyNoInteractions(step1Validator);
        }

        @Test
        void shouldThrowForbiddenOperationWhenUserIsNotOwner() {
            UUID anotherUserId = UUID.randomUUID();

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            assertThrows(
                    ForbidenOperationException.class,
                    () -> solicitationService.saveStep1(
                            anotherUserId,
                            solicitationId,
                            "Title",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION
                    )
            );

            verify(solicitationRepository, never())
                    .save(any());

            verifyNoInteractions(step1Validator);
        }

        @Test
        void shouldThrowSolicitationNotEditableWhenSavingNonDraftSolicitation() {
            solicitation.setStatus(Solicitation.Status.SUBMITTED);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            assertThrows(
                    SolicitationNotEditableException.class,
                    () -> solicitationService.saveStep1(
                            userId,
                            solicitationId,
                            "Title",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION
                    )
            );

            verify(solicitationRepository, never())
                    .save(any());

            verifyNoInteractions(step1Validator);
        }

        @Test
        void shouldCompleteStep1Successfully() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            when(solicitationRepository.save(any(Solicitation.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            var result = solicitationService.completeStep1(
                    userId,
                    solicitationId,
                    "  Instalação elétrica  ",
                    "  Preciso realizar uma instalação elétrica na residência.  ",
                    Solicitation.ServiceType.INSTALLATION
            );

            ArgumentCaptor<Solicitation> captor =
                    ArgumentCaptor.forClass(Solicitation.class);

            verify(step1Validator).validate(solicitation);

            verify(solicitationRepository)
                    .save(captor.capture());

            Solicitation savedSolicitation = captor.getValue();

            assertEquals(
                    "Instalação elétrica",
                    savedSolicitation.getTitle()
            );

            assertEquals(
                    "Preciso realizar uma instalação elétrica na residência.",
                    savedSolicitation.getDescription()
            );

            assertEquals(
                    Solicitation.ServiceType.INSTALLATION,
                    savedSolicitation.getType()
            );

            assertSame(savedSolicitation, result);
        }

        @Test
        void shouldNotSaveWhenStep1ValidationFails() {
            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            var errors = List.of(
                    new FieldException.Field(
                            "title",
                            "Title must have between 3 and 80 characters"
                    )
            );

            doThrow(new FieldException("Invalid Operation", errors))
                    .when(step1Validator)
                    .validate(any(Solicitation.class));

            FieldException exception = assertThrows(
                    FieldException.class,
                    () -> solicitationService.completeStep1(
                            userId,
                            solicitationId,
                            "",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION
                    )
            );

            assertEquals("Invalid Operation", exception.getMessage());

            assertEquals(1, exception.getFields().size());

            assertEquals(
                    "title",
                    exception.getFields().getFirst().field()
            );

            verify(step1Validator)
                    .validate(solicitation);

            verify(solicitationRepository, never())
                    .save(any());
        }

        @Test
        void shouldNotValidateOrSaveWhenCompletingNonDraftSolicitation() {
            solicitation.setStatus(Solicitation.Status.SUBMITTED);

            when(solicitationRepository.findById(solicitationId))
                    .thenReturn(Optional.of(solicitation));

            assertThrows(
                    SolicitationNotEditableException.class,
                    () -> solicitationService.completeStep1(
                            userId,
                            solicitationId,
                            "Title",
                            "Description with enough characters",
                            Solicitation.ServiceType.INSTALLATION
                    )
            );

            verifyNoInteractions(step1Validator);

            verify(solicitationRepository, never())
                    .save(any());
        }
    }
}