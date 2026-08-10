package com.deiconnect.security;

import com.deiconnect.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_EMPLOYEE_ID = "eid";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.getExpirationMs();
    }

    public String generateToken(Long userId, String email, String employeeId, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_EMPLOYEE_ID, employeeId)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public Long getUserId(Claims claims) {
        Number value = claims.get(CLAIM_USER_ID, Number.class);
        return value == null ? null : value.longValue();
    }

    public String getEmployeeId(Claims claims) {
        return claims.get(CLAIM_EMPLOYEE_ID, String.class);
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    public Role getRole(Claims claims) {
        return Role.valueOf(claims.get(CLAIM_ROLE, String.class));
    }
}
