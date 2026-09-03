package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.InvalidOperation;
import dev.viniciusjmr.servicerequest.domain.exception.ResourceNotFoundException;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.Solicitation;
import dev.viniciusjmr.servicerequest.domain.repository.AnalystCoverageRepository;
import dev.viniciusjmr.servicerequest.domain.repository.SolicitationRepository;
import dev.viniciusjmr.servicerequest.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AnalystService {

    private final AnalystCoverageRepository analystCoverageRepository;
    private final SolicitationRepository solicitationRepository;
    private final UserRepository userRepository;

    public AnalystService(AnalystCoverageRepository analystCoverageRepository, SolicitationRepository solicitationRepository, UserRepository userRepository) {
        this.analystCoverageRepository = analystCoverageRepository;
        this.solicitationRepository = solicitationRepository;
        this.userRepository = userRepository;
    }

    public List<Solicitation> getSubmittedSolicitations(UUID analystId, Role role) {
        if (role == Role.ADMIN) {
            return solicitationRepository.findAllByStatus(Solicitation.Status.SUBMITTED);
        }

        return solicitationRepository
                .findAllByAnalystCoverage(analystId)
                .stream()
                .filter(s -> s.getStatus() == Solicitation.Status.SUBMITTED).toList();
    }

    public Solicitation getSubmittedSolicitation(UUID analystId, Role role, UUID solicitationId) {
        return findSolicitationForAnalysis(analystId, role, solicitationId);
    }

    public void startSolicitation(UUID analystId, Role role, UUID solicitationId) {
        var user = userRepository.findById(analystId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var solicitation = findSolicitationForAnalysis(analystId, role, solicitationId);

        if (!solicitation.getStatus().equals(Solicitation.Status.SUBMITTED)) {
            throw new InvalidOperation("Solicitation review can only start when status is Submitted");
        }

        solicitation.setStatus(Solicitation.Status.IN_REVIEW);

        solicitationRepository.save(solicitation);
    }


    public Solicitation decideSolicitation(
            UUID analystId,
            Role role,
            UUID solicitationId,
            Solicitation.Status decision,
            String comment
    ) {
        var user = userRepository.findById(analystId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var solicitation = findSolicitationForAnalysis(analystId, role, solicitationId);

        var status = solicitation.getStatus();

        if (!status.equals(Solicitation.Status.SUBMITTED) && !status.equals(Solicitation.Status.IN_REVIEW)) {
            throw new InvalidOperation("Solicitation Can only be analyzed when status is Submitted or In Review");
        }

        solicitation.setAnalyzedBy(user);
        solicitation.setAnalyzedAt(Instant.now());
        solicitation.setStatus(decision);
        solicitation.setAnalyzisComment(comment);

        return solicitationRepository.save(solicitation);
    }

    private Solicitation findSolicitationForAnalysis(UUID analystId, Role role, UUID solicitationId) {
        if (role == Role.ADMIN) {
            return solicitationRepository.findById(solicitationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));
        }

        return solicitationRepository.findByAnalystCoverageAndId(analystId, solicitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitation not found"));
    }
}
