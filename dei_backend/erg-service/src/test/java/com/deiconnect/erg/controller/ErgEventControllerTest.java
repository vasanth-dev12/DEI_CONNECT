package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.CreateEventRequest;
import com.deiconnect.erg.dto.EventResponse;
import com.deiconnect.erg.dto.EventParticipationResponse;
import com.deiconnect.erg.enums.EventStatus;
import com.deiconnect.erg.enums.EventType;
import com.deiconnect.erg.service.ErgEventService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErgEventController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class ErgEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ErgEventService eventService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "ERG_LEAD")
    void create_Success_AsErgLead() throws Exception {
        CreateEventRequest request = new CreateEventRequest("Monthly Meeting", EventType.WORKSHOP, LocalDate.now(), 50, BigDecimal.valueOf(100.0));
        EventResponse response = new EventResponse(10L, 1L, "Monthly Meeting", EventType.WORKSHOP, LocalDate.now(), 0, BigDecimal.valueOf(100.0), EventStatus.PLANNED, Instant.now(), Instant.now());

        when(eventService.create(eq(1L), any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/ergs/1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value(10L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void create_Forbidden_AsEmployee() throws Exception {
        CreateEventRequest request = new CreateEventRequest("Monthly Meeting", EventType.WORKSHOP, LocalDate.now(), 50, BigDecimal.valueOf(100.0));

        mockMvc.perform(post("/api/ergs/1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void participate_Success_AsEmployee() throws Exception {
        EventParticipationResponse response = new EventParticipationResponse(100L, 5L, "Monthly Meeting", 50L, "Emp Name", LocalDate.now());
        when(eventService.participate(1L, 5L)).thenReturn(response);

        mockMvc.perform(post("/api/ergs/1/events/5/participate")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(100L));
    }
}
