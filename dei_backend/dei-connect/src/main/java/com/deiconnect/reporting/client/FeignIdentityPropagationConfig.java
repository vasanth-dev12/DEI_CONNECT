package com.deiconnect.reporting.client;

import com.deiconnect.security.SecurityUtils;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignIdentityPropagationConfig {

    @Value("${app.gateway.secret:g4Tew4y-1nt3rn4l-9f3XzP7mQ2wL8cR}")
    private String internalSecret;

    @Bean
    public RequestInterceptor identityPropagationInterceptor() {
        return template -> SecurityUtils.getCurrentPrincipal().ifPresent(principal -> {
            template.header("X-Internal-Auth", internalSecret);
            template.header("X-User-Id", String.valueOf(principal.getId()));
            if (principal.getRole() != null) {
                template.header("X-User-Role", principal.getRole().name());
            }
            if (principal.getEmail() != null) {
                template.header("X-User-Email", principal.getEmail());
            }
            if (principal.getEmployeeId() != null) {
                template.header("X-User-EmployeeId", principal.getEmployeeId());
            }
        });
    }
}
