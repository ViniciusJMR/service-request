package dev.viniciusjmr.servicerequest.domain.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "states",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_states_code",
                        columnNames = "code"
                )
        }
)
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 2)
    private String code;

    @Column(nullable = false)
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(id, state.id) && Objects.equals(code, state.code) && Objects.equals(name, state.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }
}
