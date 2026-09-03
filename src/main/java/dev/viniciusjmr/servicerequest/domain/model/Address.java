package dev.viniciusjmr.servicerequest.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class Address {

    @Column(length = 8)
    private String cep;

    @Column(length = 20)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(length = 120)
    private String street;

    @Column(length = 80)
    private String neighborhood;

    @Column(length = 80)
    private String city;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    public Address() {}

    public Address(String cep, String number, String complement, String street, String neighborhood, String city, State state) {
        this.cep = cep;
        this.number = number;
        this.complement = complement;
        this.street = street;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
