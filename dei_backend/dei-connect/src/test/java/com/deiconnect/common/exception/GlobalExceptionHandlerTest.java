package com.deiconnect.common.exception;

import com.deiconnect.common.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void handleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 1L);
        ResponseEntity<ApiError> response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User not found with id: 1", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
    }

    @Test
    void handleForbiddenDomain() {
        ForbiddenOperationException ex = new ForbiddenOperationException("Denied");
        ResponseEntity<ApiError> response = handler.handleForbiddenDomain(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Denied", response.getBody().message());
    }

    @Test
    void handlePrivacy() {
        PrivacyThresholdViolationException ex = new PrivacyThresholdViolationException("Privacy violation");
        ResponseEntity<ApiError> response = handler.handlePrivacy(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Privacy violation", response.getBody().message());
    }

    @Test
    void handleConflict() {
        ConflictException ex = new ConflictException("Conflict detected");
        ResponseEntity<ApiError> response = handler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflict detected", response.getBody().message());
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access is denied", response.getBody().message());
    }

    @Test
    void handleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ApiError> response = handler.handleBadCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void handleGeneric() {
        Exception ex = new RuntimeException("Any generic error");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
