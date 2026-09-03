package dev.viniciusjmr.servicerequest.domain.repository;

import dev.viniciusjmr.servicerequest.domain.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Integer> {

    Optional<State> findByCode(String code);
}
