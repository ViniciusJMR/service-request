package dev.viniciusjmr.servicerequest.domain.service.cep;

import java.util.Optional;

public interface SearchCep {

    // CEP should already be normalized
    Optional<CEPModel> search(String cep);


    static String normalizeCep(String cep) {
        if (cep == null) {
            return "";
        }

        return cep.replaceAll("\\D", "");
    }
}
