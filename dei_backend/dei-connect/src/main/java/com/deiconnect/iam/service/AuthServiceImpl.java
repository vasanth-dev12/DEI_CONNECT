package com.deiconnect.iam.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.iam.dto.AuthResponse;
import com.deiconnect.iam.dto.LoginRequest;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        DeiUserPrincipal principal = (DeiUserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));

        String token = tokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getEmployeeId(), user.getRole());
        auditLogWriter.record(user.getId(), "LOGIN", "User", user.getId());

        return AuthResponse.of(token, user);
    }
}
