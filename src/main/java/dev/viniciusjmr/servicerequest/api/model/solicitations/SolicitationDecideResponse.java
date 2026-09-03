package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.time.Instant;

public record SolicitationDecideResponse(
        Solicitation.Status status,
        String analysisComment,
        String analyzedBy,
        Instant analyzedAt
) {
    public static SolicitationDecideResponse from(Solicitation solicitation) {
        return new SolicitationDecideResponse(
                solicitation.getStatus(),
                solicitation.getAnalyzisComment(),
                solicitation.getAnalyzedBy().getName(),
                solicitation.getAnalyzedAt()
        );
    }
}
