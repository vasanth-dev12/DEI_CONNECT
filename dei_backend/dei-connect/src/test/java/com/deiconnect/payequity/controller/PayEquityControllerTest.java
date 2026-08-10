package com.deiconnect.payequity.controller;

import com.deiconnect.payequity.dto.PayEquityAnalysisRequest;
import com.deiconnect.payequity.dto.PayEquityAnalysisResponse;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.ControlVariable;
import com.deiconnect.payequity.enums.PayDimension;
import com.deiconnect.payequity.service.PayEquityAnalysisService;
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

import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deiconnect.security.SecurityConfig;

@WebMvcTest(controllers = PayEquityController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PayEquityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayEquityAnalysisService service;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "HR_BIZ_PARTNER")
    void create_Success_AsHR() throws Exception {
        PayEquityAnalysisRequest request = new PayEquityAnalysisRequest("Q1 2026", PayDimension.GENDER, Set.of(ControlVariable.GRADE, ControlVariable.ROLE));
        PayEquityAnalysisResponse response = new PayEquityAnalysisResponse(1L, "Q1 2026", PayDimension.GENDER, Set.of(ControlVariable.GRADE, ControlVariable.ROLE), 0.0, 0.0, 1.0, 10L, "HR Name", AnalysisStatus.DRAFT, Instant.now(), Instant.now());

        when(service.createAnalysis(any(PayEquityAnalysisRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pay-equity/analyses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.analysisPeriod").value("Q1 2026"));
    }

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void create_Forbidden_AsManager() throws Exception {
        PayEquityAnalysisRequest request = new PayEquityAnalysisRequest("Q1 2026", PayDimension.GENDER, Set.of(ControlVariable.GRADE, ControlVariable.ROLE));

        mockMvc.perform(post("/api/pay-equity/analyses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
