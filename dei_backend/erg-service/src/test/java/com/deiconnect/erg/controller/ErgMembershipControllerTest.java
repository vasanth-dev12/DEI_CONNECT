package com.deiconnect.erg.controller;

import com.deiconnect.erg.dto.MembershipResponse;
import com.deiconnect.erg.enums.MembershipRole;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.service.ErgMembershipService;
import com.deiconnect.security.HeaderAuthenticationFilter;
import com.deiconnect.security.RestAccessDeniedHandler;
import com.deiconnect.security.RestAuthenticationEntryPoint;
import com.deiconnect.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ErgMembershipController.class)
@Import({SecurityConfig.class, HeaderAuthenticationFilter.class})
class ErgMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ErgMembershipService membershipService;

    @MockBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @MockBean
    private RestAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void join_Success_AsEmployee() throws Exception {
        MembershipResponse response = new MembershipResponse(10L, 1L, 100L, "EMP100", MembershipRole.MEMBER, LocalDate.now(), MembershipStatus.ACTIVE, Instant.now());
        when(membershipService.join(1L)).thenReturn(response);

        mockMvc.perform(post("/api/ergs/1/memberships")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").value(10L));
    }

    @Test
    @WithMockUser(roles = "DEI_MANAGER")
    void join_Forbidden_AsManager() throws Exception {
        mockMvc.perform(post("/api/ergs/1/memberships")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
