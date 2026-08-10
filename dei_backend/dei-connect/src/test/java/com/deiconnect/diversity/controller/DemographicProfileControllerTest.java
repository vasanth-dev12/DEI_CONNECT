package com.deiconnect.diversity.controller;

import com.deiconnect.diversity.dto.DemographicProfileRequest;
import com.deiconnect.diversity.dto.DemographicProfileResponse;
import com.deiconnect.diversity.enums.AgeGroup;
import com.deiconnect.diversity.enums.ConsentStatus;
import com.deiconnect.diversity.enums.DisabilityStatus;
import com.deiconnect.diversity.enums.Ethnicity;
import com.deiconnect.diversity.enums.Gender;
import com.deiconnect.diversity.enums.VeteranStatus;
import com.deiconnect.diversity.service.DemographicProfileService;
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
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deiconnect.security.SecurityConfig;

@WebMvcTest(controllers = DemographicProfileController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DemographicProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DemographicProfileService service;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_Success_AsEmployee() throws Exception {
        DemographicProfileRequest request = new DemographicProfileRequest(Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED);
        DemographicProfileResponse response = new DemographicProfileResponse(1L, "EMP100", Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED, Instant.now(), Instant.now());

        when(service.createOwn(any(DemographicProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/demographic-profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").value(1L))
                .andExpect(jsonPath("$.gender").value("FEMALE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_Forbidden_AsAdmin() throws Exception {
        DemographicProfileRequest request = new DemographicProfileRequest(Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED);

        mockMvc.perform(post("/api/demographic-profiles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getOwn_Success_AsEmployee() throws Exception {
        DemographicProfileResponse response = new DemographicProfileResponse(1L, "EMP100", Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED, Instant.now(), Instant.now());
        when(service.getOwn()).thenReturn(response);

        mockMvc.perform(get("/api/demographic-profiles/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(1L));
    }
}
