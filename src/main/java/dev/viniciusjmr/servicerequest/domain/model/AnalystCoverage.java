package dev.viniciusjmr.servicerequest.domain.model;

import jakarta.persistence.*;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "analyst_coverage")
public class AnalystCoverage {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToMany
    @JoinTable(
            name = "analyst_coverage_states",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "state_id")
    )
    private Set<State> states;

    public AnalystCoverage(User user, Set<State> states) {
        this.user = user;
        this.states = states;
    }

    public UUID getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public Set<State> getStates() {
        return states;
    }
}
