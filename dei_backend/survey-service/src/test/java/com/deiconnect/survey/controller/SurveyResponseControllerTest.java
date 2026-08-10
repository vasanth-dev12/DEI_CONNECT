package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.AnswerItem;
import com.deiconnect.survey.dto.SubmitAcknowledgement;
import com.deiconnect.survey.dto.SubmitSurveyRequest;
import com.deiconnect.survey.service.SurveyResponseService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SurveyResponseController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class SurveyResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SurveyResponseService responseService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void submit_Success_AsEmployee() throws Exception {
        SubmitSurveyRequest request = new SubmitSurveyRequest(List.of(new AnswerItem(10L, 5)));
        SubmitAcknowledgement response = new SubmitAcknowledgement(1L, true, "Success message");

        when(responseService.submit(eq(1L), any(SubmitSurveyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/surveys/1/responses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.surveyId").value(1L))
                .andExpect(jsonPath("$.message").value("Success message"));
    }

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void submit_Forbidden_AsManager() throws Exception {
        SubmitSurveyRequest request = new SubmitSurveyRequest(List.of(new AnswerItem(10L, 5)));

        mockMvc.perform(post("/api/surveys/1/responses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
