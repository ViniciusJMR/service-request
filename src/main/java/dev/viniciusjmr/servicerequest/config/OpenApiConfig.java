package dev.viniciusjmr.servicerequest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI serviceRequestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Solicitation API")
                        .version("v1")
                        .description("API for managing service solicitations"))
                .tags(List.of(
                        new Tag()
                                .name("Auth")
                                .description("Autenticacao e cadastro de clientes"),
                        new Tag()
                                .name("Admin")
                                .description("Operacoes administrativas, incluindo criacao de usuarios internos e coverage de analistas"),
                        new Tag()
                                .name("Solicitacoes - Cliente")
                                .description("Fluxo do cliente para criar, preencher e submeter solicitacoes"),
                        new Tag()
                                .name("Solicitacoes - Analise")
                                .description("Busca e analise de solicitacoes por ANALYST ou ADMIN")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(
                                BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}
