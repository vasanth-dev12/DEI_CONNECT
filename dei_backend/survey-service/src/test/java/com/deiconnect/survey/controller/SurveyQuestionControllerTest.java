package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SurveyDimension;
import com.deiconnect.survey.service.SurveyQuestionService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SurveyQuestionController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class SurveyQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SurveyQuestionService questionService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void add_Success_AsManager() throws Exception {
        CreateQuestionRequest request = new CreateQuestionRequest("Are you happy?", QuestionType.LIKERT_SCALE, SurveyDimension.BELONGING, true, 1);
        QuestionResponse response = new QuestionResponse(10L, 1L, "Are you happy?", QuestionType.LIKERT_SCALE, SurveyDimension.BELONGING, true, 1, 5L, "Manager Name");

        when(questionService.add(eq(1L), any(CreateQuestionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/surveys/1/questions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.questionId").value(10L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void add_Forbidden_AsEmployee() throws Exception {
        CreateQuestionRequest request = new CreateQuestionRequest("Are you happy?", QuestionType.LIKERT_SCALE, SurveyDimension.BELONGING, true, 1);

        mockMvc.perform(post("/api/surveys/1/questions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
