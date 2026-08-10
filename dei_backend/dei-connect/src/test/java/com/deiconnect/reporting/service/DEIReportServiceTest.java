package com.deiconnect.reporting.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.diversity.mapper.RepresentationSnapshotMapper;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.diversity.repository.RepresentationSnapshotRepository;
import com.deiconnect.goal.repository.DEIGoalRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.service.NotificationEmitter;
import com.deiconnect.payequity.repository.PayEquityAnalysisRepository;
import com.deiconnect.payequity.repository.PayGapFlagRepository;
import com.deiconnect.reporting.client.ErgClient;
import com.deiconnect.reporting.client.SurveyClient;
import com.deiconnect.reporting.entity.DEIReport;
import com.deiconnect.reporting.enums.ReportStatus;
import com.deiconnect.reporting.mapper.DEIReportMapper;
import com.deiconnect.reporting.repository.DEIReportRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DEIReportServiceTest {

    @Mock private DEIReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private ErgClient ergClient;
    @Mock private DEIGoalRepository goalRepository;
    @Mock private SurveyClient surveyClient;
    @Mock private RepresentationSnapshotRepository snapshotRepository;
    @Mock private PayEquityAnalysisRepository payAnalysisRepository;
    @Mock private PayGapFlagRepository payFlagRepository;
    @Mock private DemographicProfileRepository demographicProfileRepository;
    @Mock private DEIReportMapper mapper;
    @Mock private RepresentationSnapshotMapper snapshotMapper;
    @Mock private PrivacyProperties privacyProperties;
    @Mock private AuditLogWriter auditLogWriter;
    @Mock private NotificationEmitter notificationEmitter;

    private DEIReportService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new DEIReportServiceImpl(reportRepository, userRepository, ergClient, goalRepository, surveyClient, snapshotRepository, payAnalysisRepository, payFlagRepository, demographicProfileRepository, mapper, snapshotMapper, privacyProperties, auditLogWriter, notificationEmitter);
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
    void getReportById_Success_WhenHRAccessesPublishedReportOfAnotherOwner() {
        mockAuthentication(10L, Role.HR_BIZ_PARTNER);

        DEIReport report = new DEIReport();
        report.setId(1L);
        report.setStatus(ReportStatus.PUBLISHED);
        User otherOwner = new User();
        otherOwner.setId(20L);
        otherOwner.setRole(Role.ADMIN);
        report.setCreatedBy(otherOwner);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(mapper.toResponse(report)).thenReturn(null);

        assertDoesNotThrow(() -> service.getReportById(1L));
    }

    @Test
    void getReportById_ThrowsResourceNotFound_WhenNonAdminAccessesDraftReport() {
        mockAuthentication(30L, Role.EXECUTIVE);

        DEIReport report = new DEIReport();
        report.setId(1L);
        report.setStatus(ReportStatus.DRAFT);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThrows(ResourceNotFoundException.class, () -> service.getReportById(1L));
    }

    @Test
    void getReportById_Success_WhenAdminAccessesDraftReport() {
        mockAuthentication(100L, Role.ADMIN);

        DEIReport report = new DEIReport();
        report.setId(1L);
        report.setStatus(ReportStatus.DRAFT);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(mapper.toResponse(report)).thenReturn(null);

        assertDoesNotThrow(() -> service.getReportById(1L));
    }
}
