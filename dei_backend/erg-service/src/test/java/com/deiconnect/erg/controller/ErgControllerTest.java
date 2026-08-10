package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.CreateErgRequest;
import com.deiconnect.erg.dto.ErgResponse;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.service.ErgService;
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

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErgController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class ErgControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ErgService ergService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void create_Success_AsManager() throws Exception {
        CreateErgRequest request = new CreateErgRequest("Women in Tech", ErgFocus.GENDER, "Mission text", 20L, 30L, LocalDate.now());
        ErgResponse response = new ErgResponse(1L, "Women in Tech", ErgFocus.GENDER, "Mission text", 20L, 30L, 10, LocalDate.now(), ErgStatus.ACTIVE, Instant.now(), Instant.now(), 5L, "Manager Name");

        when(ergService.create(any(CreateErgRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/ergs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ergId").value(1L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_Forbidden_AsEmployee() throws Exception {
        CreateErgRequest request = new CreateErgRequest("Women in Tech", ErgFocus.GENDER, "Mission text", 20L, 30L, LocalDate.now());

        mockMvc.perform(post("/api/ergs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
