package com.deiconnect.diversity.controller;

import com.deiconnect.diversity.dto.GenerateSnapshotRequest;
import com.deiconnect.diversity.dto.GenerateSnapshotResult;
import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.diversity.service.RepresentationSnapshotService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deiconnect.security.SecurityConfig;

@WebMvcTest(controllers = RepresentationSnapshotController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RepresentationSnapshotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepresentationSnapshotService service;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void generate_Success_AsManager() throws Exception {
        GenerateSnapshotRequest request = new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER);
        GenerateSnapshotResult result = new GenerateSnapshotResult(DemographicDimension.GENDER, 10L, List.of(), 0);

        when(service.generate(any(GenerateSnapshotRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/representation-snapshots/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalConsentedConsidered").value(10L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void generate_Forbidden_AsEmployee() throws Exception {
        GenerateSnapshotRequest request = new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER);

        mockMvc.perform(post("/api/representation-snapshots/generate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void publish_Success_AsManager() throws Exception {
        RepresentationSnapshotResponse response = new RepresentationSnapshotResponse(1L, LocalDate.now(), null, null, DemographicDimension.GENDER, "MALE", 10, 100.0, SnapshotStatus.PUBLISHED, false);
        when(service.publish(1L)).thenReturn(response);

        mockMvc.perform(put("/api/representation-snapshots/1/publish")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.snapshotId").value(1L));
    }

    @Test
    @WithMockUser(roles = "EXECUTIVE")
    void publish_Forbidden_AsExecutive() throws Exception {
        mockMvc.perform(put("/api/representation-snapshots/1/publish")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
