package com.deiconnect.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    private final SecretKey key;
    private final String internalSecret;

    public JwtValidationGatewayFilterFactory(
            @Value("${app.jwt.secret:K9f3XzP7mQ2wL8cR5tY1uV6nB0aD4HsE}") String secret,
            @Value("${app.gateway.secret:g4Tew4y-1nt3rn4l-9f3XzP7mQ2wL8cR}") String internalSecret) {
        super(Config.class);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.internalSecret = internalSecret;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            if (path.contains("/internal/")) {
                return onError(exchange, "Not found", HttpStatus.NOT_FOUND);
            }

            if (path.contains("/api/auth/login")
                    || path.contains("/api/auth/register")
                    || path.contains("/v3/api-docs")
                    || path.contains("/swagger-ui")
                    || path.contains("/webjars")) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                Long userId = null;
                Number uidNum = claims.get("uid", Number.class);
                if (uidNum != null) {
                    userId = uidNum.longValue();
                }

                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                String employeeId = claims.get("eid", String.class);

                if (userId == null || role == null) {
                    return onError(exchange, "Invalid JWT claims", HttpStatus.UNAUTHORIZED);
                }

                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .header("X-User-EmployeeId", employeeId != null ? employeeId : "")
                        .header("X-Internal-Auth", internalSecret)
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                return onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}
