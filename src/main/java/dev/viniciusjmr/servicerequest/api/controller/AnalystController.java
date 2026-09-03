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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Solicitacoes - Analise",
        description = "Busca e analise de solicitacoes por ANALYST ou ADMIN"
)
@RestController
@RequestMapping("/analyst")
public class AnalystController {

    private final AnalystService analystService;

    public AnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @Operation(
            summary = "Listar solicitacoes submetidas",
            description = "Lista solicitacoes com status SUBMITTED disponiveis para o usuario autenticado. ANALYST ve apenas solicitacoes dos estados do seu coverage; ADMIN pode visualizar todas."
    )
    @GetMapping("/solicitations")
    public ResponseEntity<List<AnalystSolicitationResponse>> getSolicitations(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);
        var solicitations = analystService.getSubmittedSolicitations(analystId, role);

        var solicitationResponse = solicitations.stream().map(AnalystSolicitationResponse::from).toList();

        return ResponseEntity.ok(solicitationResponse);

    }

    @Operation(
            summary = "Buscar solicitacoes",
            description = """
                    Busca solicitacoes no Elasticsearch com filtros opcionais por texto, status, tipo de servico, prioridade, estado, periodo, pagina e ordenacao.
                    O parametro status deve ser enviado como CSV, por exemplo: SUBMITTED,IN_REVIEW.
                    dateFrom e dateTo filtram createdAt usando datas no formato yyyy-MM-dd.
                    Para ANALYST, o parametro state e ignorado e substituido pelos estados configurados no coverage.
                    Para ADMIN, state e opcional e pode filtrar qualquer estado.
                    """
    )
    @GetMapping("/solicitations/search")
    public ResponseEntity<SearchPageResponse<SolicitationSearchResponse>> searchSolicitations(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
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

    @Operation(
            summary = "Buscar solicitacao para analise",
            description = "Retorna uma solicitacao especifica disponivel para analise. ANALYST deve ter coverage do estado da solicitacao; ADMIN pode acessar qualquer solicitacao."
    )
    @GetMapping("/solicitations/{id}")
    public ResponseEntity<AnalystSolicitationResponse> getSolicitation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);

        var solicitation = analystService.getSubmittedSolicitation(analystId, role, id);

        return ResponseEntity.ok(AnalystSolicitationResponse.from(solicitation));
    }

    @Operation(
            summary = "Iniciar analise",
            description = "Altera uma solicitacao SUBMITTED para IN_REVIEW, marcando o inicio da analise."
    )
    @PostMapping("/solicitations/{id}/start")
    public ResponseEntity<?> startSolicitation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        var analystId = UUID.fromString(jwt.getSubject());
        var role = getRole(jwt);

        analystService.startSolicitation(analystId, role, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Audit(action = "SOLICITATION_DECIDE")
    @Operation(
            summary = "Decidir solicitacao",
            description = "Registra a decisao final da analise. A decisao deve ser APPROVE ou REJECT."
    )
    @PostMapping("/solicitations/{id}/decide")
    public ResponseEntity<SolicitationDecideResponse> decideSolicitation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
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
