package dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import dev.viniciusjmr.servicerequest.api.model.solicitations.SolicitationSearchRequest;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.model.SolicitationDocument;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.repository.SolicitationDocumentRepository;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.SolicitationIndexingService;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.exception.SearchIndexUnavailableException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SolicitationElasticsearchService implements SolicitationIndexingService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final DateTimeFormatter ELASTICSEARCH_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                    .withZone(ZoneOffset.UTC);

    private final SolicitationDocumentRepository solicitationDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;


    public SolicitationElasticsearchService(
            SolicitationDocumentRepository solicitationDocumentRepository,
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.solicitationDocumentRepository = solicitationDocumentRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public void index(Solicitation solicitation) throws SearchIndexUnavailableException {
        try {
            solicitationDocumentRepository.save(SolicitationDocument.from(solicitation));
        } catch (Exception e) {
            throw new SearchIndexUnavailableException("Could not index solicitation", e);
        }
    }

    @Override
    public Page<SolicitationDocument> search(
            SolicitationSearchRequest request,
            Role role,
            List<String> analystAllowedStates
    ) throws SearchIndexUnavailableException {
        try {
            var pageable = PageRequest.of(
                    Math.max(request.page(), DEFAULT_PAGE),
                    request.size() > 0 ? request.size() : DEFAULT_SIZE,
                    parseSort(request.sort())
            );

            var bool = QueryBuilders.bool();

            addTextSearch(bool, request);
            addTermsFilter(bool, "status", enumNames(request.status()));
            addTermFilter(bool, "serviceType", request.serviceType() != null ? request.serviceType().name() : null);
            addTermFilter(bool, "priority", request.priority() != null ? request.priority().name() : null);
            addStateFilter(bool, role, analystAllowedStates, request.state());
            addCreatedAtRangeFilter(bool, request);

            var query = NativeQuery.builder()
                    .withQuery(q -> q.bool(bool.build()))
                    .withPageable(pageable)
                    .build();

            var hits = elasticsearchOperations.search(query, SolicitationDocument.class);

            var items = hits.stream()
                    .map(SearchHit::getContent)
                    .toList();

            return new PageImpl<>(items, pageable, hits.getTotalHits());
        } catch (Exception e) {
            throw new SearchIndexUnavailableException("Could not search solicitations", e);
        }
    }

    private void addTextSearch(BoolQuery.Builder bool, SolicitationSearchRequest request) {
        if (request.q() == null || request.q().isBlank()) {
            return;
        }

        var value = "*" + request.q().trim() + "*";

        bool.must(q -> q.bool(text -> text
                .should(should -> should.wildcard(wildcard -> wildcard
                        .field("title")
                        .value(value)
                        .caseInsensitive(true)
                ))
                .should(should -> should.wildcard(wildcard -> wildcard
                        .field("description")
                        .value(value)
                        .caseInsensitive(true)
                ))
                .minimumShouldMatch("1")
        ));
    }

    private void addTermFilter(BoolQuery.Builder bool, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        bool.filter(q -> q.term(term -> term
                .field(field)
                .value(value)
        ));
    }

    private void addTermsFilter(BoolQuery.Builder bool, String field, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        bool.filter(q -> q.terms(terms -> terms
                .field(field)
                .terms(value -> value.value(
                        values.stream()
                                .map(FieldValue::of)
                                .toList()
                ))
        ));
    }

    private void addStateFilter(
            BoolQuery.Builder bool,
            Role role,
            List<String> analystAllowedStates,
            String requestedState
    ) {
        if (role == Role.ANALYST) {
            if (analystAllowedStates == null || analystAllowedStates.isEmpty()) {
                bool.filter(q -> q.matchNone(matchNone -> matchNone));
                return;
            }

            addTermsFilter(bool, "state", analystAllowedStates);
            return;
        }

        addTermFilter(bool, "state", requestedState);
    }

    private void addCreatedAtRangeFilter(BoolQuery.Builder bool, SolicitationSearchRequest request) {
        if (request.dateFrom() == null && request.dateTo() == null) {
            return;
        }

        bool.filter(q -> q.range(range -> range.date(date -> {
            date.field("createdAt");

            if (request.dateFrom() != null) {
                date.gte(formatDateTime(request.dateFrom().atStartOfDay().toInstant(ZoneOffset.UTC)));
            }

            if (request.dateTo() != null) {
                date.lt(formatDateTime(request.dateTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
            }

            return date;
        })));
    }

    private List<String> enumNames(List<? extends Enum<?>> values) {
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .map(Enum::name)
                .toList();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        var direction = sort.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, "createdAt");
    }

    private String formatDateTime(Instant instant) {
        return ELASTICSEARCH_DATE_TIME_FORMATTER.format(instant);
    }
}
