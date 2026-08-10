package com.deiconnect.security;

import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityUtilsTest {

    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    @Test
    void testGetCurrentPrincipal_WhenEmpty() {
        Optional<DeiUserPrincipal> principal = SecurityUtils.getCurrentPrincipal();
        assertFalse(principal.isPresent());
    }

    @Test
    void testGetCurrentPrincipal_WhenAuthenticated() {
        DeiUserPrincipal expectedPrincipal = new DeiUserPrincipal(1L, "EMP101", "test@test.com", "pass", Role.EMPLOYEE, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(expectedPrincipal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        Optional<DeiUserPrincipal> principal = SecurityUtils.getCurrentPrincipal();
        assertTrue(principal.isPresent());
        assertEquals(expectedPrincipal.getId(), principal.get().getId());
        assertEquals(expectedPrincipal.getRole(), principal.get().getRole());
    }

    @Test
    void testRequireCurrentPrincipal_ThrowsForbiddenException_WhenEmpty() {
        assertThrows(ForbiddenOperationException.class, SecurityUtils::requireCurrentPrincipal);
    }

    @Test
    void testGetCurrentUserId_WhenEmpty() {
        assertNull(SecurityUtils.getCurrentUserId());
    }

    @Test
    void testGetCurrentRole_WhenEmpty() {
        assertNull(SecurityUtils.getCurrentRole());
    }

    @Test
    void testHasRole() {
        DeiUserPrincipal expectedPrincipal = new DeiUserPrincipal(1L, "EMP101", "test@test.com", "pass", Role.ADMIN, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(expectedPrincipal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertTrue(SecurityUtils.hasRole(Role.ADMIN));
        assertFalse(SecurityUtils.hasRole(Role.EMPLOYEE));
    }

    @Test
    void testRequireOwnershipOrRole_Success_Owner() {
        DeiUserPrincipal principal = new DeiUserPrincipal(1L, "EMP101", "test@test.com", "pass", Role.EMPLOYEE, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertDoesNotThrow(() -> SecurityUtils.requireOwnershipOrRole(1L, Role.ADMIN));
    }

    @Test
    void testRequireOwnershipOrRole_Success_OverrideRole() {
        DeiUserPrincipal principal = new DeiUserPrincipal(2L, "EMP102", "test@test.com", "pass", Role.ADMIN, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertDoesNotThrow(() -> SecurityUtils.requireOwnershipOrRole(1L, Role.ADMIN));
    }

    @Test
    void testRequireOwnershipOrRole_ThrowsForbiddenException() {
        DeiUserPrincipal principal = new DeiUserPrincipal(2L, "EMP102", "test@test.com", "pass", Role.EMPLOYEE, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        assertThrows(ForbiddenOperationException.class, () -> SecurityUtils.requireOwnershipOrRole(1L, Role.ADMIN));
    }
}
