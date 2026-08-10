package com.deiconnect.iam.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.iam.dto.UserResponse;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.mapper.UserMapper;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogWriter auditLogWriter;

    private UserService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, userMapper, passwordEncoder, auditLogWriter);
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

    private UserResponse buildUserResponse(Long id) {
        return new UserResponse(
                id,
                "EMP100",
                "John Doe",
                "john@test.com",
                Role.EMPLOYEE,
                2L,
                DepartmentName.SOFTWARE_ENGINEERING,
                3L,
                UserStatus.ACTIVE,
                10L,
                "Manager Name",
                20L,
                "HR Name",
                Instant.now(),
                Instant.now(),
                null,
                null
        );
    }

    @Test
    void getById_Success() {
        mockAuthentication(1L, Role.ADMIN);
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse expected = buildUserResponse(1L);
        when(userMapper.toResponse(eq(user), anyBoolean())).thenReturn(expected);

        UserResponse actual = service.getById(1L);

        assertNotNull(actual);
        assertEquals(1L, actual.userId());
    }

    @Test
    void getById_ThrowsResourceNotFound_WhenUserDoesNotExist() {
        mockAuthentication(1L, Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void list_AsHR_ReturnsOnlyAssignedEmployees() {
        mockAuthentication(20L, Role.HR_BIZ_PARTNER);

        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(100L);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByHr_IdAndRole(20L, Role.EMPLOYEE, pageable)).thenReturn(userPage);

        UserResponse expected = buildUserResponse(100L);
        when(userMapper.toResponse(eq(user), anyBoolean())).thenReturn(expected);

        Page<UserResponse> actual = service.list(Role.EMPLOYEE, pageable);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        assertEquals(100L, actual.getContent().get(0).userId());
    }

    @Test
    void list_AsManager_ReturnsOnlyReportingEmployees() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(100L);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByManager_IdAndRole(10L, Role.EMPLOYEE, pageable)).thenReturn(userPage);

        UserResponse expected = buildUserResponse(100L);
        when(userMapper.toResponse(eq(user), anyBoolean())).thenReturn(expected);

        Page<UserResponse> actual = service.list(Role.EMPLOYEE, pageable);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        assertEquals(100L, actual.getContent().get(0).userId());
    }

    @Test
    void list_AsAdmin_ReturnsAllEmployees() {
        mockAuthentication(1L, Role.ADMIN);

        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        user.setId(100L);
        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByRole(Role.EMPLOYEE, pageable)).thenReturn(userPage);

        UserResponse expected = buildUserResponse(100L);
        when(userMapper.toResponse(eq(user), anyBoolean())).thenReturn(expected);

        Page<UserResponse> actual = service.list(Role.EMPLOYEE, pageable);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        assertEquals(100L, actual.getContent().get(0).userId());
    }
}
