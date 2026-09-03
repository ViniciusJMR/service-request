package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.solicitations.AnalystSolicitationResponse;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationDecideRequest;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationDecideResponse;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.service.AnalystService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/analyst")
public class AnalystController {

    private final AnalystService analystService;

    public AnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @GetMapping("/solicitations")
    public ResponseEntity<List<AnalystSolicitationResponse>> getSolicitations(@AuthenticationPrincipal Jwt jwt ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var solicitations = analystService.getSubmittedSolicitations(analystId);

        var solicitationResponse = solicitations.stream().map(AnalystSolicitationResponse::from).toList();

        return ResponseEntity.ok(solicitationResponse);

    }

    @GetMapping("/solicitations/{id}")
    public ResponseEntity<AnalystSolicitationResponse> getSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());

        var solicitation = analystService.getSubmittedSolicitation(analystId, id);

        return ResponseEntity.ok(AnalystSolicitationResponse.from(solicitation));
    }

    @PostMapping("/solicitations/{id}/start")
    public ResponseEntity<?> startSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());

        analystService.startSolicitation(analystId, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/solicitations/{id}/decide")
    public ResponseEntity<SolicitationDecideResponse> decideSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid SolicitationDecideRequest body
    ) {
        var analystId = UUID.fromString(jwt.getSubject());


        var decision = Objects.equals(body.decision(), "APPROVE") ? Solicitation.Status.APPROVED : Solicitation.Status.REJECTED;
        var solicitation = analystService.decideSolicitation(
                analystId,
                id,
                decision,
                body.comment()
        );


        return ResponseEntity.ok(SolicitationDecideResponse.from(solicitation));
    }
}
