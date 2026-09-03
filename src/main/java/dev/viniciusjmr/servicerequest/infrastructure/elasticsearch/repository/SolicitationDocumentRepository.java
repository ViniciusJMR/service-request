package dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.repository;

import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model.SolicitationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SolicitationDocumentRepository extends ElasticsearchRepository<SolicitationDocument, UUID> {
}
