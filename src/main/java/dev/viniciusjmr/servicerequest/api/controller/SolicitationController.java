package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.solicitations.*;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.service.SolicitationService;
import dev.viniciusjmr.servicerequest.infrastructure.audit.Audit;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.IndexSolicitation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Solicitacoes - Cliente",
        description = "Fluxo do cliente para criar, preencher e submeter solicitacoes"
)
@RestController
@RequestMapping("/solicitations")
public class SolicitationController {

    private final SolicitationService solicitationService;

    public SolicitationController(SolicitationService solicitationService) {
        this.solicitationService = solicitationService;
    }

    @Operation(
            summary = "Criar solicitacao",
            description = "Cria uma solicitacao em branco para o cliente autenticado, iniciando o fluxo no step 1."
    )
    @PostMapping
    public ResponseEntity<CreateSolicitationResponse> createSolicitation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());

        var request = solicitationService.createBlankSolicitation(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSolicitationResponse.from(request));
    }


    @Operation(
            summary = "Salvar step 1",
            description = "Salva parcialmente titulo, descricao e tipo de servico da solicitacao sem exigir conclusao do step."
    )
    @PatchMapping("/{id}/step/1")
    public ResponseEntity<Step1Response> patchStep1(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step1Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());

        Solicitation.ServiceType type = null;

        if (body.serviceType() != null) {
            type = Solicitation.ServiceType.valueOf(body.serviceType());
        }

        var solicitation = solicitationService.saveStep1(
                userId,
                id,
                body.title(),
                body.description(),
                type,
                false
        );

        return ResponseEntity.ok(Step1Response.from(solicitation));
    }

    @Operation(
            summary = "Concluir step 1",
            description = "Valida e conclui o step 1 com titulo, descricao e tipo de servico."
    )
    @PostMapping("/{id}/step/1")
    public ResponseEntity<Step1Response> completeStep1(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step1Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());

        Solicitation.ServiceType type = null;

        if (body.serviceType() != null) {
            type = Solicitation.ServiceType.valueOf(body.serviceType());
        }

        var solicitation = solicitationService.saveStep1(
                userId,
                id,
                body.title(),
                body.description(),
                type,
                false
        );

        return ResponseEntity.ok(Step1Response.from(solicitation));
    }

    @Operation(
            summary = "Salvar step 2",
            description = "Salva parcialmente os dados de endereco da solicitacao a partir do CEP informado."
    )
    @PatchMapping("/{id}/step/2")
    public ResponseEntity<Step2Response> patchStep2(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody Step2Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        var solicitation = solicitationService.saveStep2(
                userId,
                id,
                body.cep(),
                body.number(),
                body.complement(),
                false
        );

        return ResponseEntity.ok(Step2Response.from(solicitation));
    }

    @Operation(
            summary = "Concluir step 2",
            description = "Valida e conclui o step 2 com CEP, numero e complemento, preenchendo endereco por consulta de CEP."
    )
    @PostMapping("/{id}/step/2")
    public ResponseEntity<Step2Response> completeStep2(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step2Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        var solicitation = solicitationService.saveStep2(
                userId,
                id,
                body.cep(),
                body.number(),
                body.complement(),
                true
        );

        return ResponseEntity.ok(Step2Response.from(solicitation));
    }

    @Operation(
            summary = "Salvar step 3",
            description = "Salva parcialmente prioridade, data preferencial, valor estimado e aceite dos termos."
    )
    @PatchMapping("/{id}/step/3")
    public ResponseEntity<Step3Response> saveStep3(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step3Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        Solicitation.Priority priority = null;

        if (body.priority() != null) {
            priority = Solicitation.Priority.valueOf(body.priority());
        }

        var solicitation = solicitationService.saveStep3(
                userId,
                id,
                priority,
                body.preferredDate(),
                body.estimatedValue(),
                body.termsAccepted(),
                false
        );

        return ResponseEntity.ok(Step3Response.from(solicitation));
    }

    @Operation(
            summary = "Concluir step 3",
            description = "Valida e conclui o step 3 com prioridade, data preferencial, valor estimado e aceite dos termos."
    )
    @PostMapping("/{id}/step/3")
    public ResponseEntity<Step3Response> completeStep3(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step3Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        Solicitation.Priority priority = null;

        if (body.priority() != null) {
                priority = Solicitation.Priority.valueOf(body.priority());
        }

        var solicitation = solicitationService.saveStep3(
                userId,
                id,
                priority,
                body.preferredDate(),
                body.estimatedValue(),
                body.termsAccepted(),
                true
        );

        return ResponseEntity.ok(Step3Response.from(solicitation));
    }

    @Audit(action = "SOLICITATION_SUBMIT")
    @Operation(
            summary = "Submeter solicitacao",
            description = "Submete a solicitacao preenchida para analise, alterando o status para SUBMITTED."
    )
    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitResponse> submit(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var userId = UUID.fromString(jwt.getSubject());

        var solicitation = solicitationService.submit(userId, id);

        return ResponseEntity.ok().body(SubmitResponse.from(solicitation));
    }
}
