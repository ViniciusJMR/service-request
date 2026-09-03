package dev.viniciusjmr.servicerequest.domain.service;

import dev.viniciusjmr.servicerequest.domain.exception.FieldException;
import dev.viniciusjmr.servicerequest.domain.model.AnalystCoverage;
import dev.viniciusjmr.servicerequest.domain.model.Role;
import dev.viniciusjmr.servicerequest.domain.model.State;
import dev.viniciusjmr.servicerequest.domain.model.User;
import dev.viniciusjmr.servicerequest.domain.repository.AnalystCoverageRepository;
import dev.viniciusjmr.servicerequest.domain.repository.StateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserService userService;
    private final StateRepository stateRepository;
    private final AnalystCoverageRepository analystCoverageRepository;

    public AdminService(UserService userService, StateRepository stateRepository, AnalystCoverageRepository analystCoverageRepository) {
        this.userService = userService;
        this.stateRepository = stateRepository;
        this.analystCoverageRepository = analystCoverageRepository;
    }

    public AnalystCoverage createUser(
            String name,
            String email,
            String password,
            Role role,
            Set<String> codes
    ) {
        var states = findStatesByCodes(codes);

        var user = userService.createUser(name, email, password, role);

        var analystCoverage = new AnalystCoverage(
                user,
                states
        );

        return analystCoverageRepository.save(analystCoverage);
    }


    private Set<State> findStatesByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }

        var states = stateRepository.findAllByCodeIn(codes);

        var foundCodes = states.stream()
                .map(State::getCode)
                .collect(Collectors.toSet());

        var invalidCodes = codes.stream()
                .filter(code -> !foundCodes.contains(code))
                .toList();

        if (!invalidCodes.isEmpty()) {
            throw new FieldException(
                    "Invalid Fields",
                    List.of(new FieldException.Field(
                            "states",
                            "Invalid states: " + String.join(", ", invalidCodes)
                    ))
            );
        }

        return states;
    }
}
