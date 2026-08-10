package com.deiconnect.security;

import com.deiconnect.common.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.gateway.secret:g4Tew4y-1nt3rn4l-9f3XzP7mQ2wL8cR}")
    private String internalSecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String internalAuth = request.getHeader("X-Internal-Auth");
        if (!StringUtils.hasText(internalAuth) || !internalSecret.equals(internalAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userIdStr = request.getHeader("X-User-Id");
        String roleStr = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");
        String employeeId = request.getHeader("X-User-EmployeeId");

        if (StringUtils.hasText(userIdStr) && StringUtils.hasText(roleStr)) {
            try {
                Long userId = Long.valueOf(userIdStr);
                Role role = Role.valueOf(roleStr);

                DeiUserPrincipal principal = DeiUserPrincipal.fromToken(
                        userId,
                        StringUtils.hasText(employeeId) ? employeeId : null,
                        email,
                        role
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                logger.debug("Failed to parse security headers: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
