package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.entity.ERGMembership;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.enums.MembershipStatus;
import com.deiconnect.erg.mapper.ErgMembershipMapper;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErgMembershipServiceTest {

    @Mock private ErgMembershipRepository membershipRepository;
    @Mock private ErgService ergService;
    @Mock private ErgMembershipMapper membershipMapper;
    @Mock private AuditLogWriter auditLogWriter;

    private ErgMembershipService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new ErgMembershipServiceImpl(membershipRepository, ergService, membershipMapper, auditLogWriter);
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
    void join_ThrowsConflict_WhenAlreadyActiveMember() {
        mockAuthentication(100L, Role.EMPLOYEE);

        ERG erg = new ERG();
        erg.setId(1L);
        erg.setStatus(ErgStatus.ACTIVE);
        when(ergService.findOrThrow(1L)).thenReturn(erg);

        ERGMembership existing = new ERGMembership();
        existing.setStatus(MembershipStatus.ACTIVE);

        when(membershipRepository.findByErg_IdAndEmployeeUserId(1L, 100L))
                .thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> service.join(1L));
    }

    @Test
    void leave_ThrowsResourceNotFound_WhenNotMember() {
        mockAuthentication(100L, Role.EMPLOYEE);

        when(membershipRepository.findByErg_IdAndEmployeeUserId(1L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.leave(1L));
    }
}
