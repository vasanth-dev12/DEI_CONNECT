package com.deiconnect.diversity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.PrivacyThresholdViolationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.diversity.dto.GenerateSnapshotRequest;
import com.deiconnect.diversity.dto.GenerateSnapshotResult;
import com.deiconnect.diversity.dto.SnapshotRunResponse;
import com.deiconnect.diversity.entity.RepresentationSnapshot;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.diversity.mapper.RepresentationSnapshotMapper;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.diversity.repository.RepresentationSnapshotRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.enums.NotificationCategory;
import com.deiconnect.notification.service.NotificationEmitter;
import com.deiconnect.security.DeiUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepresentationSnapshotServiceTest {

    private static final Long MANAGER_ID = 77L;

    @Mock
    private RepresentationSnapshotRepository snapshotRepository;
    @Mock
    private DemographicProfileRepository profileRepository;
    @Mock
    private PrivacyProperties privacyProperties;
    @Mock
    private AuditLogWriter auditLogWriter;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationEmitter notificationEmitter;

    private final RepresentationSnapshotMapper mapper = new RepresentationSnapshotMapper();

    private RepresentationSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new RepresentationSnapshotServiceImpl(
                snapshotRepository, profileRepository, mapper, privacyProperties, auditLogWriter,
                userRepository, notificationEmitter);
        lenient().when(privacyProperties.getDefaultMinGroupSize()).thenReturn(5);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static void withPrincipal(Role role, Long userId) {
        DeiUserPrincipal principal = DeiUserPrincipal.fromToken(userId, "E" + userId, "u@x.io", role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private static RepresentationSnapshot row(Long id, String group, Integer count, SnapshotStatus status) {
        RepresentationSnapshot s = new RepresentationSnapshot();
        s.setId(id);
        s.setSnapshotDate(LocalDate.now());
        s.setDimension(DemographicDimension.GENDER);
        s.setGroupName(group);
        s.setCount(count);
        s.setPercentage(count == null ? null : count.doubleValue());
        s.setStatus(status);
        s.setCreatorManagerId(MANAGER_ID);
        return s;
    }

    private void noPreviousRun() {
        lenient().when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of());
    }

    private void echoSaves() {
        when(snapshotRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void generate_SuppressesGroupBelowThreshold() {
        noPreviousRun();
        when(profileRepository.aggregateByGender(null)).thenReturn(List.<Object[]>of(
                new Object[]{"MALE", 10L},
                new Object[]{"FEMALE", 3L}));
        echoSaves();

        GenerateSnapshotResult result = service.generate(
                new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER));

        assertEquals(13L, result.totalConsentedConsidered(), "percentage base includes suppressed groups");
        assertEquals(1, result.suppressedGroupCount());
        assertEquals(1, result.snapshots().size(), "only the group above the threshold is persisted");
    }

    @Test
    void generate_WorksForEveryDimension_NotJustGender() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        for (DemographicDimension dim : DemographicDimension.values()) {
            reset(profileRepository, snapshotRepository);
            noPreviousRun();
            echoSaves();
            stubForManager(dim);

            GenerateSnapshotResult result = service.generate(
                    new GenerateSnapshotRequest(LocalDate.now(), null, dim));

            assertEquals(dim, result.dimension(), "dimension echoed for " + dim);
            assertEquals(9L, result.totalConsentedConsidered(), "aggregated for " + dim);
            assertEquals(1, result.snapshots().size(), "one group persisted for " + dim);
        }
    }

    private void stubForManager(DemographicDimension dim) {
        List<Object[]> rows = List.<Object[]>of(new Object[]{"GROUP_A", 9L});
        switch (dim) {
            case GENDER -> when(profileRepository.aggregateByGenderForManager(null, MANAGER_ID)).thenReturn(rows);
            case ETHNICITY -> when(profileRepository.aggregateByEthnicityForManager(null, MANAGER_ID)).thenReturn(rows);
            case DISABILITY -> when(profileRepository.aggregateByDisabilityForManager(null, MANAGER_ID)).thenReturn(rows);
            case VETERAN -> when(profileRepository.aggregateByVeteranForManager(null, MANAGER_ID)).thenReturn(rows);
            case AGE_GROUP -> when(profileRepository.aggregateByAgeGroupForManager(null, MANAGER_ID)).thenReturn(rows);
        }
    }

    @Test
    void generate_NarrowsByDepartment_DerivingTheIdFromTheName() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        Long deptId = DepartmentName.HR.getId();
        noPreviousRun();
        when(profileRepository.aggregateByGenderForManager(deptId, MANAGER_ID))
                .thenReturn(List.<Object[]>of(new Object[]{"FEMALE", 8L}));
        echoSaves();

        GenerateSnapshotRequest request =
                new GenerateSnapshotRequest(LocalDate.now(), DepartmentName.HR, DemographicDimension.GENDER);
        service.generate(request);

        verify(profileRepository).aggregateByGenderForManager(deptId, MANAGER_ID);
        verify(profileRepository, never()).aggregateByGenderForManager(null, MANAGER_ID);
        verify(snapshotRepository).findDistribution(
                request.snapshotDate(), DemographicDimension.GENDER, deptId, MANAGER_ID);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RepresentationSnapshot>> saved = ArgumentCaptor.forClass(List.class);
        verify(snapshotRepository).saveAll(saved.capture());
        assertEquals(deptId, saved.getValue().get(0).getDepartmentId());
    }

    @Test
    void generate_ReplacesAPreviousDraftRun_SoDimensionsAreNotDuplicated() {
        RepresentationSnapshot stale = row(99L, "MALE", 10, SnapshotStatus.DRAFT);
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(stale));
        when(profileRepository.aggregateByGender(null)).thenReturn(List.<Object[]>of(new Object[]{"MALE", 10L}));
        echoSaves();

        service.generate(new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER));

        verify(snapshotRepository).deleteAll(List.of(stale));
        verify(snapshotRepository).flush();
    }

    @Test
    void generate_RefusesToOverwriteAPublishedRun() {
        when(snapshotRepository.findDistribution(any(), any(), any(), any()))
                .thenReturn(List.of(row(99L, "MALE", 10, SnapshotStatus.PUBLISHED)));

        assertThrows(ConflictException.class, () -> service.generate(
                new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER)));
        verify(snapshotRepository, never()).deleteAll(any());
        verify(snapshotRepository, never()).save(any(RepresentationSnapshot.class));
    }

    @Test
    void searchRuns_CollapsesAGenerationRunIntoASingleRecord() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        when(snapshotRepository.findAllForRuns(null, null, null, MANAGER_ID)).thenReturn(List.of(
                row(1L, "FEMALE", 9, SnapshotStatus.DRAFT),
                row(2L, "MALE", 10, SnapshotStatus.DRAFT)));

        var page = service.searchRuns(null, null, null, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements(), "one run, not one row per group");
        SnapshotRunResponse run = page.getContent().get(0);
        assertEquals(DemographicDimension.GENDER, run.dimension());
        assertEquals(2, run.groupCount());
        assertEquals(19, run.totalHeadCount(), "9 + 10");
        assertEquals(0, run.suppressedGroupCount());
        assertEquals(SnapshotStatus.DRAFT, run.status());
        assertEquals(List.of("FEMALE", "MALE"), run.groups().stream().map(g -> g.groupName()).toList());
    }

    @Test
    void searchRuns_SeparatesRunsThatDifferOnlyByDepartment() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        RepresentationSnapshot orgWide = row(1L, "FEMALE", 11, SnapshotStatus.DRAFT);
        RepresentationSnapshot hrOnly = row(2L, "FEMALE", 6, SnapshotStatus.DRAFT);
        hrOnly.setDepartmentId(DepartmentName.HR.getId());
        when(snapshotRepository.findAllForRuns(null, null, null, MANAGER_ID))
                .thenReturn(List.of(orgWide, hrOnly));

        var page = service.searchRuns(null, null, null, PageRequest.of(0, 20));

        assertEquals(2, page.getTotalElements());
        List<DepartmentName> scopes = page.getContent().stream()
                .map(SnapshotRunResponse::departmentName)
                .toList();
        assertNull(scopes.get(0), "organisation-wide run has no department");
        assertEquals(DepartmentName.HR, scopes.get(1));
    }

    @Test
    void searchRuns_ReportsPublishedOnlyWhenEveryRowIsPublished() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        when(snapshotRepository.findAllForRuns(null, null, null, MANAGER_ID)).thenReturn(List.of(
                row(1L, "FEMALE", 9, SnapshotStatus.PUBLISHED),
                row(2L, "MALE", 10, SnapshotStatus.DRAFT)));

        var page = service.searchRuns(null, null, null, PageRequest.of(0, 20));

        assertEquals(SnapshotStatus.DRAFT, page.getContent().get(0).status(),
                "a partially published run is still a draft");
    }

    @Test
    void publishRun_PublishesEveryPublishableGroupInOneGo() {
        RepresentationSnapshot female = row(1L, "FEMALE", 9, SnapshotStatus.DRAFT);
        RepresentationSnapshot male = row(2L, "MALE", 10, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(female));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(female, male));

        SnapshotRunResponse run = service.publishRun(1L);

        assertEquals(SnapshotStatus.PUBLISHED, female.getStatus());
        assertEquals(SnapshotStatus.PUBLISHED, male.getStatus());
        assertEquals(SnapshotStatus.PUBLISHED, run.status());
        verify(auditLogWriter).record("PUBLISH_SNAPSHOT", "RepresentationSnapshot", 1L);
    }

    @Test
    void publishRun_RejectsARunWhereEveryGroupIsBelowThreshold() {
        RepresentationSnapshot tiny = row(1L, "FEMALE", 2, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(tiny));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(tiny));

        assertThrows(PrivacyThresholdViolationException.class, () -> service.publishRun(1L));
        assertEquals(SnapshotStatus.DRAFT, tiny.getStatus());
    }

    @Test
    void deleteRun_RemovesEveryGroupRowInTheSnapshot() {
        RepresentationSnapshot female = row(1L, "FEMALE", 9, SnapshotStatus.DRAFT);
        RepresentationSnapshot male = row(2L, "MALE", 10, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(female));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(female, male));

        service.deleteRun(1L);

        verify(snapshotRepository).deleteAll(List.of(female, male));
        verify(auditLogWriter).record("DELETE_SNAPSHOT", "RepresentationSnapshot", 1L);
    }

    @Test
    void generate_RecordsTheSuppressedCountOnEverySurvivingRow() {
        noPreviousRun();
        when(profileRepository.aggregateByGender(null)).thenReturn(List.<Object[]>of(
                new Object[]{"MALE", 10L},
                new Object[]{"FEMALE", 3L},
                new Object[]{"NON_BINARY", 2L}));
        when(snapshotRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        GenerateSnapshotResult result = service.generate(
                new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER));

        assertEquals(2, result.suppressedGroupCount());
        ArgumentCaptor<List<RepresentationSnapshot>> saved = ArgumentCaptor.forClass(List.class);
        verify(snapshotRepository).saveAll(saved.capture());
        assertEquals(1, saved.getValue().size(), "only the group above the threshold is stored");
        RepresentationSnapshot stored = saved.getValue().get(0);
        assertEquals(2, stored.getSuppressedGroupCount());
        assertEquals(15, stored.getTotalConsidered(), "percentage base includes withheld people");
    }

    @Test
    void searchRuns_ReportsTheRecordedSuppressedCount_NotZero() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        RepresentationSnapshot male = row(1L, "MALE", 10, SnapshotStatus.DRAFT);
        male.setSuppressedGroupCount(2);
        male.setTotalConsidered(15);
        when(snapshotRepository.findAllForRuns(null, null, null, MANAGER_ID)).thenReturn(List.of(male));

        SnapshotRunResponse run = service.searchRuns(null, null, null, PageRequest.of(0, 20))
                .getContent().get(0);

        assertEquals(2, run.suppressedGroupCount());
        assertEquals(10, run.totalHeadCount(), "visible head count");
        assertEquals(15, run.totalConsidered(), "including withheld groups");
    }

    @Test
    void getDistribution_ReportsTheRecordedSuppressedCount() {
        RepresentationSnapshot male = row(1L, "MALE", 10, SnapshotStatus.DRAFT);
        male.setSuppressedGroupCount(3);
        male.setTotalConsidered(20);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(male));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(male));

        var dist = service.getDistribution(1L);

        assertEquals(3, dist.suppressedGroupCount());
        assertEquals(20, dist.totalConsidered());
    }

    @Test
    void searchRuns_AsExecutive_IsPinnedToPublished() {
        withPrincipal(Role.EXECUTIVE, 55L);
        when(snapshotRepository.findAllForRuns(null, null, SnapshotStatus.PUBLISHED, null))
                .thenReturn(List.of(row(1L, "MALE", 10, SnapshotStatus.PUBLISHED)));

        service.searchRuns(null, null, SnapshotStatus.DRAFT, PageRequest.of(0, 20));

        verify(snapshotRepository).findAllForRuns(null, null, SnapshotStatus.PUBLISHED, null);
        verify(snapshotRepository, never()).findAllForRuns(null, null, SnapshotStatus.DRAFT, null);
    }

    @Test
    void search_AsExecutive_IsPinnedToPublished() {
        withPrincipal(Role.EXECUTIVE, 55L);
        when(snapshotRepository.search(any(), any(), eq(SnapshotStatus.PUBLISHED), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.search(null, null, null, PageRequest.of(0, 20));

        verify(snapshotRepository).search(null, null, SnapshotStatus.PUBLISHED, null, PageRequest.of(0, 20));
    }

    @Test
    void getById_AsExecutive_HidesADraftAsNotFound() {
        withPrincipal(Role.EXECUTIVE, 55L);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "MALE", 10, SnapshotStatus.DRAFT)));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getById_AsExecutive_AllowsAPublishedSnapshot() {
        withPrincipal(Role.EXECUTIVE, 55L);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "MALE", 10, SnapshotStatus.PUBLISHED)));

        assertDoesNotThrow(() -> service.getById(1L));
    }

    @Test
    void getDistribution_AsExecutive_HidesADraftAsNotFound() {
        withPrincipal(Role.EXECUTIVE, 55L);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "MALE", 10, SnapshotStatus.DRAFT)));

        assertThrows(ResourceNotFoundException.class, () -> service.getDistribution(1L));
    }

    @Test
    void searchRuns_AsAdmin_IsPinnedToPublished() {
        withPrincipal(Role.ADMIN, 77L);
        when(snapshotRepository.findAllForRuns(null, null, SnapshotStatus.PUBLISHED, null))
                .thenReturn(List.of(row(1L, "MALE", 10, SnapshotStatus.PUBLISHED)));

        service.searchRuns(null, null, SnapshotStatus.DRAFT, PageRequest.of(0, 20));

        verify(snapshotRepository).findAllForRuns(null, null, SnapshotStatus.PUBLISHED, null);
        verify(snapshotRepository, never()).findAllForRuns(null, null, SnapshotStatus.DRAFT, null);
    }

    @Test
    void search_AsAdmin_IsPinnedToPublished() {
        withPrincipal(Role.ADMIN, 77L);
        when(snapshotRepository.search(any(), any(), eq(SnapshotStatus.PUBLISHED), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.search(null, null, null, PageRequest.of(0, 20));

        verify(snapshotRepository).search(null, null, SnapshotStatus.PUBLISHED, null, PageRequest.of(0, 20));
    }

    @Test
    void getById_AsAdmin_HidesADraftAsNotFound() {
        withPrincipal(Role.ADMIN, 77L);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "MALE", 10, SnapshotStatus.DRAFT)));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void getById_AsAdmin_AllowsAPublishedSnapshot() {
        withPrincipal(Role.ADMIN, 77L);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "MALE", 10, SnapshotStatus.PUBLISHED)));

        assertDoesNotThrow(() -> service.getById(1L));
    }

    @Test
    void searchRuns_AsManager_StillSeesTheirOwnDrafts() {
        withPrincipal(Role.DEI_MANAGER, MANAGER_ID);
        when(snapshotRepository.findAllForRuns(null, null, SnapshotStatus.DRAFT, MANAGER_ID))
                .thenReturn(List.of(row(1L, "MALE", 10, SnapshotStatus.DRAFT)));

        var page = service.searchRuns(null, null, SnapshotStatus.DRAFT, PageRequest.of(0, 20));

        assertEquals(1, page.getTotalElements(), "a manager's own drafts remain visible to them");
    }

    @Test
    void publishRun_NotifiesActiveExecutives() {
        RepresentationSnapshot male = row(1L, "MALE", 10, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(male));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(male));
        when(userRepository.findByRole(eq(Role.EXECUTIVE), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(
                        executive("EXE001", UserStatus.ACTIVE),
                        executive("EXE002", UserStatus.INACTIVE))));

        service.publishRun(1L);

        verify(notificationEmitter).emit(eq("EXE001"), eq(NotificationCategory.REPORT), anyString());
        verify(notificationEmitter, never()).emit(eq("EXE002"), any(), anyString());
    }

    @Test
    void publishRun_DoesNotNotifyAgainWhenAlreadyPublished() {
        RepresentationSnapshot male = row(1L, "MALE", 10, SnapshotStatus.PUBLISHED);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(male));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(male));

        service.publishRun(1L);

        verifyNoInteractions(notificationEmitter);
    }

    @Test
    void generate_DoesNotNotifyAnyone_DraftsAreNotAnnounced() {
        noPreviousRun();
        when(profileRepository.aggregateByGender(null)).thenReturn(List.<Object[]>of(new Object[]{"MALE", 10L}));
        when(snapshotRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        service.generate(new GenerateSnapshotRequest(LocalDate.now(), null, DemographicDimension.GENDER));

        verifyNoInteractions(notificationEmitter);
    }

    @Test
    void publishRun_SucceedsEvenWhenNotifyingFails() {
        RepresentationSnapshot male = row(1L, "MALE", 10, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(male));
        when(snapshotRepository.findDistribution(any(), any(), any(), any())).thenReturn(List.of(male));
        when(userRepository.findByRole(eq(Role.EXECUTIVE), any()))
                .thenThrow(new RuntimeException("directory unavailable"));

        assertDoesNotThrow(() -> service.publishRun(1L));
        assertEquals(SnapshotStatus.PUBLISHED, male.getStatus());
    }

    private static User executive(String employeeId, UserStatus status) {
        User u = new User();
        u.setEmployeeId(employeeId);
        u.setStatus(status);
        return u;
    }

    @Test
    void publish_ThrowsPrivacyThresholdViolation_WhenCountBelowThreshold() {
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(row(1L, "FEMALE", 3, SnapshotStatus.DRAFT)));

        assertThrows(PrivacyThresholdViolationException.class, () -> service.publish(1L));
    }

    @Test
    void publish_Success_WhenCountAboveThreshold() {
        RepresentationSnapshot snapshot = row(1L, "MALE", 12, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(snapshot));
        when(snapshotRepository.save(snapshot)).thenReturn(snapshot);

        service.publish(1L);

        assertEquals(SnapshotStatus.PUBLISHED, snapshot.getStatus());
        verify(auditLogWriter).record("PUBLISH_SNAPSHOT", "RepresentationSnapshot", 1L);
    }

    @Test
    void getDistribution_ReturnsTheWholeRunWithTotals() {
        RepresentationSnapshot female = row(1L, "FEMALE", 9, SnapshotStatus.DRAFT);
        RepresentationSnapshot male = row(2L, "MALE", 10, SnapshotStatus.DRAFT);
        RepresentationSnapshot tiny = row(3L, "NON_BINARY", 2, SnapshotStatus.DRAFT);
        when(snapshotRepository.findById(1L)).thenReturn(Optional.of(female));
        when(snapshotRepository.findDistribution(any(), any(), any(), any()))
                .thenReturn(List.of(female, male, tiny));

        var dist = service.getDistribution(1L);

        assertEquals(3, dist.groupCount());
        assertEquals(1, dist.suppressedGroupCount(), "the group of 2 is withheld");
        assertEquals(19, dist.totalHeadCount(), "suppressed group excluded from the visible total");
    }
}
