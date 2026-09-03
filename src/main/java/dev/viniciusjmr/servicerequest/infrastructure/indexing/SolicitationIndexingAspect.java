package dev.viniciusjmr.servicerequest.infrastructure.indexing;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.exception.SearchIndexUnavailableException;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SolicitationIndexingAspect {

    private static final Logger log = LoggerFactory.getLogger(SolicitationIndexingAspect.class);
    private final SolicitationIndexingService solicitationIndexService;

    public SolicitationIndexingAspect(SolicitationIndexingService solicitationElasticsearchService) {
        this.solicitationIndexService = solicitationElasticsearchService;
    }

    @AfterReturning(
            pointcut = "@annotation(IndexSolicitation)",
            returning = "result"
    )
    public void index(Object result) {
        try{
            if (result instanceof Solicitation solicitation) {
                solicitationIndexService.index(solicitation);
            }
        } catch (SearchIndexUnavailableException ex) {
            log.warn(
                    "Failed to index solicitation. error={}",
                    ex.getMessage(),
                    ex
            );
        }
    }
}
