package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.CreateSurveyRequest;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.enums.SurveyType;
import com.deiconnect.survey.service.SurveyService;
import com.deiconnect.security.HeaderAuthenticationFilter;
import com.deiconnect.security.RestAccessDeniedHandler;
import com.deiconnect.security.RestAuthenticationEntryPoint;
import com.deiconnect.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SurveyController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class SurveyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SurveyService surveyService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_Success_AsAdmin() throws Exception {
        CreateSurveyRequest request = new CreateSurveyRequest("Annual Survey", SurveyType.ANNUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), true, 5, List.of());
        SurveyResponse response = new SurveyResponse(1L, "Annual Survey", SurveyType.ANNUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), true, 5, SurveyStatus.DRAFT, null, null, List.of());

        when(surveyService.create(any(CreateSurveyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/surveys")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.surveyId").value(1L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_Forbidden_AsEmployee() throws Exception {
        CreateSurveyRequest request = new CreateSurveyRequest("Annual Survey", SurveyType.ANNUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), true, 5, List.of());

        mockMvc.perform(post("/api/surveys")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
