package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.erg.client.UserClient;
import com.deiconnect.erg.client.UserResponse;
import com.deiconnect.erg.dto.EventParticipationResponse;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.entity.ERGEvent;
import com.deiconnect.erg.entity.ERGEventParticipation;
import com.deiconnect.erg.entity.ERGMembership;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.mapper.ErgEventMapper;
import com.deiconnect.erg.repository.ERGEventParticipationRepository;
import com.deiconnect.erg.repository.ErgEventRepository;
import com.deiconnect.erg.repository.ErgMembershipRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErgEventServiceTest {

    @Mock private ErgEventRepository eventRepository;
    @Mock private ErgService ergService;
    @Mock private ErgEventMapper eventMapper;
    @Mock private AuditLogWriter auditLogWriter;
    @Mock private ErgMembershipRepository membershipRepository;
    @Mock private UserClient userClient;
    @Mock private ERGEventParticipationRepository participationRepository;

    private ErgEventService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new ErgEventServiceImpl(eventRepository, ergService, eventMapper, auditLogWriter, membershipRepository, userClient, participationRepository);
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(Long id, Role role) {
        DeiUserPrincipal principal = new DeiUserPrincipal(id, "EMP100", "test@test.com", "pass", role, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void participate_ThrowsForbidden_WhenNotActiveMember() {
        mockAuthentication(100L, Role.EMPLOYEE);

        ERGEvent event = new ERGEvent();
        event.setId(5L);
        ERG erg = new ERG();
        erg.setId(1L);
        event.setErg(erg);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));

        when(membershipRepository.findByErg_IdAndEmployeeUserId(1L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenOperationException.class, () -> service.participate(1L, 5L));
    }

    @Test
    void participate_Success_WhenActiveMember() {
        mockAuthentication(100L, Role.EMPLOYEE);

        ERGEvent event = new ERGEvent();
        event.setId(5L);
        ERG erg = new ERG();
        erg.setId(1L);
        event.setErg(erg);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));

        ERGMembership membership = new ERGMembership();
        membership.setStatus(MembershipStatus.ACTIVE);
        when(membershipRepository.findByErg_IdAndEmployeeUserId(1L, 100L))
                .thenReturn(Optional.of(membership));

        when(participationRepository.existsByEvent_IdAndEmployeeUserId(5L, 100L)).thenReturn(false);

        UserResponse user = new UserResponse(100L, "EMP100", "Employee Name", "emp@test.com", Role.EMPLOYEE, 10L, 20L, "ACTIVE", 10L, "Manager Name", 20L, "HR Name", Instant.now(), Instant.now());
        when(userClient.getByIdInternal(100L)).thenReturn(user);

        when(participationRepository.save(any(ERGEventParticipation.class))).thenAnswer(i -> {
            ERGEventParticipation p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        EventParticipationResponse res = service.participate(1L, 5L);

        assertNotNull(res);
        assertEquals(5L, res.eventId());
        assertEquals(100L, res.employeeId());
        verify(eventRepository).save(event);
    }
}
