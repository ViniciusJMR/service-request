package dev.viniciusjmr.servicerequest.infrastructure.viacep;

import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;
import dev.viniciusjmr.servicerequest.domain.service.cep.SearchCep;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Service
public class ViaCepSearchCep implements SearchCep {

    private final RestClient restClient;

    public ViaCepSearchCep() {
        this.restClient = RestClient.builder()
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }

    @Override
    public Optional<CEPModel> search(String cep) {
        if (cep.length() != 8) {
            return Optional.empty();
        }

        try {
            var response = restClient.get()
                    .uri("/{cep}/json", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);

            if (response == null || Boolean.TRUE.equals(response.erro())) {
                return Optional.empty();
            }

            return Optional.of(response.toCEPModel());
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }
}
