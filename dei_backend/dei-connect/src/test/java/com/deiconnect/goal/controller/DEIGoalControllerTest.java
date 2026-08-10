package com.deiconnect.goal.controller;

import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import com.deiconnect.goal.service.DEIGoalService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DEIGoalController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DEIGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DEIGoalService service;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void getById_Success_AsManager() throws Exception {
        GoalResponse response = new GoalResponse(1L, "Increase diversity", GoalDimension.GENDER, "FEMALE", 15.0, 30.0, 2027, 10L, "Manager Ten", GoalStatus.ACTIVE, Instant.now(), Instant.now());
        when(service.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/goals/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(1L))
                .andExpect(jsonPath("$.goalName").value("Increase diversity"))
                .andExpect(jsonPath("$.ownerName").value("Manager Ten"));
    }
}
