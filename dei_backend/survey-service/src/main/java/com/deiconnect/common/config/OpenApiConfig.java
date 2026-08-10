package com.deiconnect.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI deiConnectOpenAPI() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/")))
                .info(new Info()
                        .title("DEIConnect API")
                        .version("0.0.1")
                        .description("""
                                Diversity, Equity & Inclusion Management Platform — REST backend.

                                Stateless JWT + RBAC. Privacy guardrails (k-anonymity / minimum group
                                size, consent enforcement, anonymised survey responses, audit logging,
                                least privilege, ownership scoping) are enforced in the service layer.

                                Authenticate via POST /api/auth/login, then click 'Authorize' and paste
                                the returned token.""")
                        .contact(new Contact().name("DEIConnect Team").email("dei-platform@corp.com"))
                        .license(new License().name("Proprietary")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by /api/auth/login")));
    }
}
