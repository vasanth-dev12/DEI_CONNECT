package com.deiconnect.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway Services")
                        .version("1.0")
                        .description("Unified entrypoint for the DEIConnect platforms. Exposes and routes all microservices."));
    }
}
