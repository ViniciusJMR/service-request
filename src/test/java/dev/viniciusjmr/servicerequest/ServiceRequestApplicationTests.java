package dev.viniciusjmr.servicerequest;

import com.jayway.jsonpath.JsonPath;
import dev.viniciusjmr.servicerequest.domain.service.cep.CEPModel;
import dev.viniciusjmr.servicerequest.domain.service.cep.SearchCep;
import dev.viniciusjmr.servicerequest.infrastructure.elasticsearch.repository.SolicitationDocumentRepository;
import dev.viniciusjmr.servicerequest.infrastructure.indexing.SolicitationIndexingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:service_request_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true",
        "spring.data.elasticsearch.repositories.enabled=false",
        "app.initial-admin.name=Admin",
        "app.initial-admin.email=admin@email.com",
        "app.initial-admin.password=123456"
})
class ServiceRequestApplicationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private SolicitationIndexingService solicitationIndexingService;

    @MockitoBean
    private SolicitationDocumentRepository solicitationDocumentRepository;

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockitoBean
    private SearchCep searchCep;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldCompleteBasicSolicitationFlow() throws Exception {
        when(searchCep.search(anyString())).thenReturn(Optional.of(new CEPModel(
                "01001000",
                "Praca da Se",
                "",
                "Se",
                "Sao Paulo",
                "SP"
        )));

        var adminToken = login("admin@email.com", "123456");
        var clientEmail = "client@email.com";
        var analystEmail = "analyst@email.com";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Client Test",
                                  "email": "%s",
                                  "password": "123456"
                                }
                                """.formatted(clientEmail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CLIENT"));

        mockMvc.perform(post("/admin/users")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Analyst Test",
                                  "email": "%s",
                                  "password": "123456",
                                  "role": "ANALYST",
                                  "states": ["SP"]
                                }
                                """.formatted(analystEmail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ANALYST"))
                .andExpect(jsonPath("$.states[0]").value("SP"));

        var clientToken = login(clientEmail, "123456");
        var solicitationId = createSolicitation(clientToken);

        mockMvc.perform(post("/solicitations/{id}/step/1", solicitationId)
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Instalacao eletrica",
                                  "description": "Preciso instalar uma nova fiacao eletrica na residencia.",
                                  "serviceType": "INSTALLATION"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId))
                .andExpect(jsonPath("$.currentStep").value(2));

        mockMvc.perform(post("/solicitations/{id}/step/2", solicitationId)
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cep": "01001-000",
                                  "number": "100",
                                  "complement": "Casa"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId))
                .andExpect(jsonPath("$.currentStep").value(3))
                .andExpect(jsonPath("$.state").value("SP"));

        mockMvc.perform(post("/solicitations/{id}/step/3", solicitationId)
                        .header("Authorization", bearer(clientToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "HIGH",
                                  "preferredDate": "2026-12-10",
                                  "estimatedValue": 250.0,
                                  "termsAccepted": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStep").value(3))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.termsAccepted").value(true));

        mockMvc.perform(post("/solicitations/{id}/submit", solicitationId)
                        .header("Authorization", bearer(clientToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        var analystToken = login(analystEmail, "123456");

        mockMvc.perform(post("/analyst/solicitations/{id}/start", solicitationId)
                        .header("Authorization", bearer(analystToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/analyst/solicitations/{id}/decide", solicitationId)
                        .header("Authorization", bearer(analystToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "comment": "Solicitacao analisada e aprovada para execucao."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.analyzedBy").value("Analyst Test"));
    }

    private String login(String email, String password) throws Exception {
        var response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private String createSolicitation(String clientToken) throws Exception {
        var response = mockMvc.perform(post("/solicitations")
                        .header("Authorization", bearer(clientToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

}
