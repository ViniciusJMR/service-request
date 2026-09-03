package dev.viniciusjmr.servicerequest.domain.repository;

import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitationRepository extends JpaRepository<Solicitation, UUID> {

    List<Solicitation> findAllByStatus(Solicitation.Status status);

    @Query("""
            SELECT s
            FROM Solicitation s
            WHERE s.status = 'SUBMITTED'
            AND s.address.state IN (
                SELECT state
                FROM AnalystCoverage ac
                JOIN ac.states state
                WHERE ac.userId = :analystId
            )
            """)
    List<Solicitation> findAllByAnalystCoverage(
            @Param("analystId") UUID analystId
    );

    @Query("""
            SELECT s
            FROM Solicitation s
            WHERE s.id = :solicitationId
            AND s.address.state IN (
                SELECT state
                FROM AnalystCoverage ac
                JOIN ac.states state
                WHERE ac.userId = :analystId
            )
            """)
    Optional<Solicitation> findByAnalystCoverageAndId(
            @Param("analystId") UUID analystId,
            @Param("solicitationId") UUID solicitationId
    );
}
