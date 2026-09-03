package dev.viniciusjmr.servicerequest.domain.service.cep;

import dev.viniciusjmr.servicerequest.domain.model.Address;
import dev.viniciusjmr.servicerequest.domain.model.State;

public class CEPModel {
    private String cep;
    private String street;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    public CEPModel(String cep, String street, String complement, String neighborhood, String city, String state) {
        this.cep = cep;
        this.street = street;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
    }

    public CEPModel() {}

    public String getCep() {
        return cep;
    }

    public String getStreet() {
        return street;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

}
