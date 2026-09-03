package dev.viniciusjmr.servicerequest.domain.repository;

import dev.viniciusjmr.servicerequest.domain.model.AnalystCoverage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalystCoverageRepository
        extends JpaRepository<AnalystCoverage, UUID> {

    @EntityGraph(attributePaths = "states")
    @Query("""
    SELECT ac
    FROM AnalystCoverage ac
    WHERE ac.userId = :userId
""")
    Optional<AnalystCoverage> findByUserIdWithStates(
            @Param("userId") UUID userId
    );
}
