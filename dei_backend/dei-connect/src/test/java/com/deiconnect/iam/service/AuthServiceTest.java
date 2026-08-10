package com.deiconnect.iam.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.iam.dto.AuthResponse;
import com.deiconnect.iam.dto.LoginRequest;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuditLogWriter auditLogWriter;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, tokenProvider, authenticationManager, auditLogWriter);
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("admin@test.com", "pass123");
        DeiUserPrincipal principal = new DeiUserPrincipal(1L, "EMP001", "admin@test.com", "pass123", Role.ADMIN, true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@test.com");
        user.setEmployeeId("EMP001");
        user.setRole(Role.ADMIN);
        user.setName("Admin Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(1L, "admin@test.com", "EMP001", Role.ADMIN))
                .thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1L, response.userId());
        assertEquals("admin@test.com", response.email());
        assertEquals("ADMIN", response.role().name());

        verify(auditLogWriter).record(1L, "LOGIN", "User", 1L);
    }

    @Test
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest("admin@test.com", "pass123");
        DeiUserPrincipal principal = new DeiUserPrincipal(1L, "EMP001", "admin@test.com", "pass123", Role.ADMIN, true);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }
}
