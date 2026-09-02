package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.solicitations.CreateSolicitationResponse;
import dev.viniciusjmr.servicerequest.api.model.solicitations.Step1Request;
import dev.viniciusjmr.servicerequest.api.model.solicitations.Step1Response;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.service.SolicitationService;
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

        var request = solicitationService.createBlankRequest(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(CreateSolicitationResponse.from(request));
    }


    @PatchMapping("/{id}/step/1")
    public ResponseEntity<Step1Response> patchStep1(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid Step1Request body
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        var solicitation = solicitationService.saveStep1(
                userId,
                id,
                body.title(),
                body.description(),
                Solicitation.ServiceType.valueOf(body.serviceType())
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
        var solicitation = solicitationService.completeStep1(
                userId,
                id,
                body.title(),
                body.description(),
                Solicitation.ServiceType.valueOf(body.serviceType())
        );

        return ResponseEntity.ok(Step1Response.from(solicitation));
    }

}
