package dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.service;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model.SolicitationDocument;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.repository.SolicitationDocumentRepository;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.SolicitationIndexingService;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.exception.SearchIndexUnavailableException;
import org.springframework.stereotype.Service;

@Service
public class SolicitationElasticsearchService implements SolicitationIndexingService {
    private final SolicitationDocumentRepository solicitationDocumentRepository;


    public SolicitationElasticsearchService(SolicitationDocumentRepository solicitationDocumentRepository) {
        this.solicitationDocumentRepository = solicitationDocumentRepository;
    }

    public void index(Solicitation solicitation) throws SearchIndexUnavailableException {
        solicitationDocumentRepository.save(SolicitationDocument.from(solicitation));
    }
}
