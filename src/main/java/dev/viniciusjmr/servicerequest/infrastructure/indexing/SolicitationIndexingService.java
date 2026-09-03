package dev.viniciusjmr.servicerequest.infrastructure.indexing;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.exception.SearchIndexUnavailableException;

public interface SolicitationIndexingService {
    void index(Solicitation solicitation) throws SearchIndexUnavailableException;
}
