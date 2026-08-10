package com.deiconnect.iam.controller;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.dto.UserResponse;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.service.UserService;
import com.deiconnect.security.JwtAuthenticationFilter;
import com.deiconnect.security.JwtTokenProvider;
import com.deiconnect.security.RestAccessDeniedHandler;
import com.deiconnect.security.RestAuthenticationEntryPoint;
import com.deiconnect.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    private UserResponse buildUserResponse(Long id) {
        return new UserResponse(
                id,
                "EMP100",
                "John Doe",
                "john@test.com",
                Role.EMPLOYEE,
                2L,
                DepartmentName.SOFTWARE_ENGINEERING,
                3L,
                UserStatus.ACTIVE,
                10L,
                "Manager Name",
                20L,
                "HR Name",
                Instant.now(),
                Instant.now(),
                null,
                null
        );
    }

    @Test
    @WithMockUser(roles = "HR_BIZ_PARTNER")
    void getById_Success_AsHR() throws Exception {
        UserResponse response = buildUserResponse(1L);
        when(userService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deactivate_Forbidden_AsEmployee() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
