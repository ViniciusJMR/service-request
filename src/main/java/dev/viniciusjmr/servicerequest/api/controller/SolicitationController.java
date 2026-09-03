package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.solicitations.*;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.service.SolicitationService;
import dev.viniciusjmr.servicerequest.infrastructure.audit.Audit;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.IndexSolicitation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/solicitations")
public class SolicitationController {

    private final SolicitationService solicitationService;

    public SolicitationController(SolicitationService solicitationService) {
        this.solicitationService = solicitationService;
    }

    @PostMapping
    public ResponseEntity<CreateSolicitationResponse> createSolicitation(@AuthenticationPrincipal Jwt jwt) {
        var userId = UUID.fromString(jwt.getSubject());

        var request = solicitationService.createBlankSolicitation(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSolicitationResponse.from(request));
    }


    @PatchMapping("/{id}/step/1")
    public ResponseEntity<Step1Response> patchStep1(
            @AuthenticationPrincipal Jwt jwt,
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

    @PostMapping("/{id}/step/1")
    public ResponseEntity<Step1Response> completeStep1(
            @AuthenticationPrincipal Jwt jwt,
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

    @PatchMapping("/{id}/step/2")
    public ResponseEntity<Step2Response> patchStep2(
            @AuthenticationPrincipal Jwt jwt,
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

    @PostMapping("/{id}/step/2")
    public ResponseEntity<Step2Response> completeStep2(
            @AuthenticationPrincipal Jwt jwt,
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

    @PatchMapping("/{id}/step/3")
    public ResponseEntity<Step3Response> saveStep3(
            @AuthenticationPrincipal Jwt jwt,
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

    @PostMapping("/{id}/step/3")
    public ResponseEntity<Step3Response> completeStep3(
            @AuthenticationPrincipal Jwt jwt,
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
    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmitResponse> submit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var userId = UUID.fromString(jwt.getSubject());

        var solicitation = solicitationService.submit(userId, id);

        return ResponseEntity.ok().body(SubmitResponse.from(solicitation));
    }
}
