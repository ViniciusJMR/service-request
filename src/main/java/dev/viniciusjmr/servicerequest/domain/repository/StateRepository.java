package dev.viniciusjmr.servicerequest.domain.repository;

import dev.viniciusjmr.servicerequest.domain.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {

    Optional<State> findByCode(String code);

    Set<State> findAllByCodeIn(Collection<String> codes);
}
