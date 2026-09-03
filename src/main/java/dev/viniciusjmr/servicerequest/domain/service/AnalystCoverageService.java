package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.model.AnalystCoverage;
import dev.viniciusjmr.servicerequest.domain.repository.AnalystCoverageRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalystCoverageService {

    private final AnalystCoverageRepository analystCoverageRepository;

    public AnalystCoverageService(AnalystCoverageRepository analystCoverageRepository) {
        this.analystCoverageRepository = analystCoverageRepository;
    }

}
