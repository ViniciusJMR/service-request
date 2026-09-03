package dev.viniciusjmr.servicerequest.api.model.solicitations;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;

import java.time.LocalDate;
import java.util.List;

public record SolicitationSearchRequest(
        String q,
        List<Solicitation.Status> status,
        Solicitation.ServiceType serviceType,
        Solicitation.Priority priority,
        String state,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int size,
        String sort
) {
}
