package dev.viniciusjmr.servicerequest.infrastructure.indexing;

import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationSearchRequest;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model.SolicitationDocument;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.exception.SearchIndexUnavailableException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SolicitationIndexingService {
    void index(Solicitation solicitation) throws SearchIndexUnavailableException;

    Page<SolicitationDocument> search(
            SolicitationSearchRequest request,
            Role role,
            List<String> analystAllowedStates
    ) throws SearchIndexUnavailableException;
}
