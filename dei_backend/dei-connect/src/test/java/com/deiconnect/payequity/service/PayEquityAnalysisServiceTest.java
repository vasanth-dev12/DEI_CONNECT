package com.deiconnect.payequity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.service.NotificationEmitter;
import com.deiconnect.payequity.dto.PayGapFlagResponse;
import com.deiconnect.payequity.dto.UpdatePayGapFlagRequest;
import com.deiconnect.payequity.entity.PayEquityAnalysis;
import com.deiconnect.payequity.entity.PayGapFlag;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.FlagStatus;
import com.deiconnect.payequity.mapper.PayEquityMapper;
import com.deiconnect.payequity.repository.PayEquityAnalysisRepository;
import com.deiconnect.payequity.repository.PayGapFlagRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayEquityAnalysisServiceTest {

    @Mock
    private PayEquityAnalysisRepository analysisRepository;
    @Mock
    private PayGapFlagRepository flagRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PayEquityMapper mapper;
    @Mock
    private PrivacyProperties privacyProperties;
    @Mock
    private AuditLogWriter auditLogWriter;
    @Mock
    private NotificationEmitter notificationEmitter;
    @Mock
    private DemographicProfileRepository demographicProfileRepository;

    private PayEquityAnalysisService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new PayEquityAnalysisServiceImpl(analysisRepository, flagRepository, userRepository, mapper, privacyProperties, auditLogWriter, notificationEmitter, demographicProfileRepository);
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(Long id, Role role) {
        DeiUserPrincipal principal = new DeiUserPrincipal(id, "EMP100", "hr@test.com", "pass", role, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void updateFlag_ThrowsForbidden_WhenAssigningEmployeeNotUnderThisHR() {
        mockAuthentication(10L, Role.HR_BIZ_PARTNER);

        PayEquityAnalysis analysis = new PayEquityAnalysis();
        analysis.setId(1L);
        analysis.setStatus(AnalysisStatus.DRAFT);
        User hrUser = new User();
        hrUser.setId(10L);
        analysis.setRunBy(hrUser);

        PayGapFlag flag = new PayGapFlag();
        flag.setId(100L);
        flag.setAnalysis(analysis);

        when(flagRepository.findById(100L)).thenReturn(Optional.of(flag));

        User employee = new User();
        employee.setId(50L);
        employee.setRole(Role.EMPLOYEE);
        User differentHr = new User();
        differentHr.setId(20L);
        employee.setHr(differentHr);

        when(userRepository.findById(50L)).thenReturn(Optional.of(employee));

        UpdatePayGapFlagRequest request = new UpdatePayGapFlagRequest(50L, FlagStatus.OPEN);

        assertThrows(ForbiddenOperationException.class, () -> service.updateFlag(1L, 100L, request));
    }
}
