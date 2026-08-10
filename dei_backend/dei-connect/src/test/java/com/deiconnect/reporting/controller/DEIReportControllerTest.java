package com.deiconnect.reporting.controller;

import com.deiconnect.reporting.dto.DEIReportRequest;
import com.deiconnect.reporting.dto.DEIReportResponse;
import com.deiconnect.reporting.enums.ReportMetric;
import com.deiconnect.reporting.enums.ReportScope;
import com.deiconnect.reporting.enums.ReportStatus;
import com.deiconnect.reporting.service.DEIReportService;
import com.deiconnect.security.JwtAuthenticationFilter;
import com.deiconnect.security.JwtTokenProvider;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DEIReportController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DEIReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DEIReportService service;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_Success_AsAdmin() throws Exception {
        DEIReportRequest request = new DEIReportRequest(ReportScope.ORGANISATION, null, Set.of(ReportMetric.REPRESENTATION_BY_DIMENSION));
        DEIReportResponse response = new DEIReportResponse(1L, ReportScope.ORGANISATION, null, Set.of(ReportMetric.REPRESENTATION_BY_DIMENSION), LocalDate.now(), ReportStatus.DRAFT, java.time.Instant.now(), java.time.Instant.now());

        when(service.createReport(any(DEIReportRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void create_Forbidden_AsManager() throws Exception {
        DEIReportRequest request = new DEIReportRequest(ReportScope.ORGANISATION, null, Set.of(ReportMetric.REPRESENTATION_BY_DIMENSION));

        mockMvc.perform(post("/api/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
