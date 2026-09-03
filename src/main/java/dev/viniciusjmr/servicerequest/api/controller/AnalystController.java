package dev.viniciusjmr.servicerequest.api.controller;

import dev.viniciusjmr.servicerequest.api.model.solicitations.AnalystSolicitationResponse;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SearchPageResponse;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationDecideRequest;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationDecideResponse;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationSearchRequest;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationSearchResponse;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.service.AnalystService;
import dev.viniciusjmr.servicerequest.infrastructure.audit.Audit;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
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
        var role = getRole(jwt);
        var solicitations = analystService.getSubmittedSolicitations(analystId, role);

        var solicitationResponse = solicitations.stream().map(AnalystSolicitationResponse::from).toList();

        return ResponseEntity.ok(solicitationResponse);

    }

    @GetMapping("/solicitations/search")
    public ResponseEntity<SearchPageResponse<SolicitationSearchResponse>> searchSolicitations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Solicitation.ServiceType serviceType,
            @RequestParam(required = false) Solicitation.Priority priority,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);
        var request = new SolicitationSearchRequest(
                q,
                parseStatus(status),
                serviceType,
                priority,
                state,
                dateFrom,
                dateTo,
                page,
                size,
                sort
        );

        var result = analystService.searchSolicitations(analystId, role, request);
        var items = result.getContent()
                .stream()
                .map(SolicitationSearchResponse::from)
                .toList();

        return ResponseEntity.ok(new SearchPageResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements()
        ));
    }

    @GetMapping("/solicitations/{id}")
    public ResponseEntity<AnalystSolicitationResponse> getSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);

        var solicitation = analystService.getSubmittedSolicitation(analystId, role, id);

        return ResponseEntity.ok(AnalystSolicitationResponse.from(solicitation));
    }

    @PostMapping("/solicitations/{id}/start")
    public ResponseEntity<?> startSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);

        analystService.startSolicitation(analystId, role, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Audit(action = "SOLICITATION_DECIDE")
    @PostMapping("/solicitations/{id}/decide")
    public ResponseEntity<SolicitationDecideResponse> decideSolicitation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @RequestBody @Valid SolicitationDecideRequest body
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);


        var decision = Objects.equals(body.decision(), "APPROVE") ? Solicitation.Status.APPROVED : Solicitation.Status.REJECTED;
        var solicitation = analystService.decideSolicitation(
                analystId,
                role,
                id,
                decision,
                body.comment()
        );


        return ResponseEntity.ok(SolicitationDecideResponse.from(solicitation));
    }

    private Role getRole(Jwt jwt) {
        return Role.valueOf(jwt.getClaimAsString("role"));
    }

    private List<Solicitation.Status> parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return List.of();
        }

        return Arrays.stream(status.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Solicitation.Status::valueOf)
                .toList();
    }
}
