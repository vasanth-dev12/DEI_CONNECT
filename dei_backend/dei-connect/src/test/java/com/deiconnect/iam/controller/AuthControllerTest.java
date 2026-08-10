package com.deiconnect.iam.controller;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.dto.AuthResponse;
import com.deiconnect.iam.dto.LoginRequest;
import com.deiconnect.iam.service.AuthService;
import com.deiconnect.security.JwtAuthenticationFilter;
import com.deiconnect.security.JwtTokenProvider;
import com.deiconnect.security.RestAccessDeniedHandler;
import com.deiconnect.security.RestAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deiconnect.security.SecurityConfig;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "pass123");
        AuthResponse response = new AuthResponse("token-123", "Bearer", 1L, "EMP001", "admin@test.com", "Admin Name", Role.ADMIN);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser
    void login_ValidationError() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
