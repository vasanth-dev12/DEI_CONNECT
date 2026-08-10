package com.deiconnect.erg.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.erg.client.UserClient;
import com.deiconnect.erg.client.UserResponse;
import com.deiconnect.erg.dto.UpdateErgRequest;
import com.deiconnect.erg.entity.ERG;
import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;
import com.deiconnect.erg.mapper.ErgMapper;
import com.deiconnect.erg.repository.ErgMembershipRepository;
import com.deiconnect.erg.repository.ErgRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErgServiceTest {

    @Mock private ErgRepository ergRepository;
    @Mock private ErgMembershipRepository membershipRepository;
    @Mock private UserClient userClient;
    @Mock private ErgMapper ergMapper;
    @Mock private AuditLogWriter auditLogWriter;

    private ErgService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new ErgServiceImpl(ergRepository, membershipRepository, userClient, ergMapper, auditLogWriter);
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
    void update_ThrowsForbidden_WhenManagerUpdatesErgCreatedByAnotherManager() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        ERG erg = new ERG();
        erg.setId(1L);
        erg.setCreatorManagerId(11L);

        when(ergRepository.findById(1L)).thenReturn(Optional.of(erg));

        UpdateErgRequest request = new UpdateErgRequest("New Name", ErgFocus.GENDER, "Mission", 20L, 30L, LocalDate.now(), ErgStatus.ACTIVE);

        assertThrows(ForbiddenOperationException.class, () -> service.update(1L, request));
    }

    @Test
    void search_ScopesEmployeeToTheirOwnManagersChapters() {
        mockAuthentication(5L, Role.EMPLOYEE);
        when(userClient.getByIdInternal(5L)).thenReturn(userWithManager(5L, 10L));
        when(ergRepository.searchVisibleToEmployee(null, null, 10L, Pageable.unpaged()))
                .thenReturn(Page.empty());

        service.search(null, null, Pageable.unpaged());

        verify(ergRepository).searchVisibleToEmployee(null, null, 10L, Pageable.unpaged());
        verify(ergRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    void search_ScopesManagerToTheirOwnChapters() {
        mockAuthentication(10L, Role.DEI_MANAGER);
        when(ergRepository.searchVisibleToEmployee(null, null, 10L, Pageable.unpaged()))
                .thenReturn(Page.empty());

        service.search(null, null, Pageable.unpaged());

        verify(ergRepository).searchVisibleToEmployee(null, null, 10L, Pageable.unpaged());
        verify(ergRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    void getById_ThrowsNotFound_WhenEmployeesManagerDoesNotRunTheChapter() {
        mockAuthentication(5L, Role.EMPLOYEE);
        when(userClient.getByIdInternal(5L)).thenReturn(userWithManager(5L, 10L));

        ERG erg = new ERG();
        erg.setId(1L);
        erg.setCreatorManagerId(11L);
        when(ergRepository.findById(1L)).thenReturn(Optional.of(erg));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getById_AllowsOrgWideChapter() {
        mockAuthentication(5L, Role.EMPLOYEE);

        ERG erg = new ERG();
        erg.setId(1L);
        erg.setCreatorManagerId(null);
        when(ergRepository.findById(1L)).thenReturn(Optional.of(erg));

        assertDoesNotThrow(() -> service.getById(1L));
    }

    private static UserResponse userWithManager(Long userId, Long managerId) {
        return new UserResponse(userId, "EMP" + userId, "Test User", "test@test.com", Role.EMPLOYEE,
                null, null, "ACTIVE", managerId, "Manager", null, null, null, null);
    }
}
