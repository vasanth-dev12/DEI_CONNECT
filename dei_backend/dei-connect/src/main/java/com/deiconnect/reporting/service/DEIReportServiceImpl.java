package com.deiconnect.reporting.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.entity.RepresentationSnapshot;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.diversity.mapper.RepresentationSnapshotMapper;
import com.deiconnect.diversity.repository.RepresentationSnapshotRepository;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.reporting.client.ErgClient;
import com.deiconnect.reporting.client.SurveyClient;
import com.deiconnect.goal.enums.GoalStatus;
import com.deiconnect.goal.repository.DEIGoalRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.enums.NotificationCategory;
import com.deiconnect.notification.service.NotificationEmitter;
import com.deiconnect.payequity.entity.PayEquityAnalysis;
import com.deiconnect.payequity.entity.PayGapFlag;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.repository.PayEquityAnalysisRepository;
import com.deiconnect.payequity.repository.PayGapFlagRepository;
import com.deiconnect.reporting.dto.DEIReportDataResponse;
import com.deiconnect.reporting.dto.DEIReportRequest;
import com.deiconnect.reporting.dto.DEIReportResponse;
import com.deiconnect.reporting.entity.DEIReport;
import com.deiconnect.reporting.enums.ReportMetric;
import com.deiconnect.reporting.enums.ReportScope;
import com.deiconnect.reporting.enums.ReportStatus;
import com.deiconnect.reporting.mapper.DEIReportMapper;
import com.deiconnect.reporting.repository.DEIReportRepository;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DEIReportServiceImpl implements DEIReportService {

    private final DEIReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ErgClient ergClient;
    private final DEIGoalRepository goalRepository;
    private final SurveyClient surveyClient;
    private final RepresentationSnapshotRepository snapshotRepository;
    private final PayEquityAnalysisRepository payAnalysisRepository;
    private final PayGapFlagRepository payFlagRepository;
    private final DemographicProfileRepository demographicProfileRepository;

    private final DEIReportMapper mapper;
    private final RepresentationSnapshotMapper snapshotMapper;
    private final PrivacyProperties privacyProperties;
    private final AuditLogWriter auditLogWriter;
    private final NotificationEmitter notificationEmitter;

    @Override
    @Transactional
    public DEIReportResponse createReport(DEIReportRequest request) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        DEIReport report = DEIReport.builder()
                .scope(request.scope())
                .scopeValue(request.scopeValue())
                .metrics(request.metrics())
                .generatedDate(LocalDate.now())
                .status(ReportStatus.DRAFT)
                .createdBy(userRepository.getReferenceById(principal.getId()))
                .build();

        report = reportRepository.save(report);
        auditLogWriter.record("CREATE_REPORT_DEFINITION", "DEIReport", report.getId());
        return mapper.toResponse(report);
    }

    @Override
    @Transactional
    public DEIReportResponse updateReport(Long id, DEIReportRequest request) {
        DEIReport report = loadReportOrThrow(id);
        checkReportAccess(report);
        if (report.getStatus() != ReportStatus.DRAFT) {
            throw new ConflictException("Cannot edit a published report");
        }

        report.setScope(request.scope());
        report.setScopeValue(request.scopeValue());
        report.setMetrics(request.metrics());
        report.setGeneratedDate(LocalDate.now());

        report = reportRepository.save(report);
        auditLogWriter.record("UPDATE_REPORT_DEFINITION", "DEIReport", report.getId());
        return mapper.toResponse(report);
    }

    @Override
    @Transactional
    public DEIReportResponse publishReport(Long id) {
        DEIReport report = loadReportOrThrow(id);
        checkReportAccess(report);
        if (report.getStatus() == ReportStatus.PUBLISHED) {
            throw new ConflictException("Report is already published");
        }

        report.setStatus(ReportStatus.PUBLISHED);
        report = reportRepository.save(report);

        String message = "DEI Report for " + report.getScope() + " (" + report.getScopeValue() + ") is now published.";
        for (Role role : List.of(Role.DEI_MANAGER, Role.HR_BIZ_PARTNER, Role.EXECUTIVE, Role.ADMIN)) {
            Page<User> usersPage = userRepository.findByRole(role, PageRequest.of(0, 1000));
            for (User u : usersPage.getContent()) {
                if (u.getStatus() == UserStatus.ACTIVE) {
                    notificationEmitter.emit(u.getEmployeeId(), NotificationCategory.REPORT, message);
                }
            }
        }

        auditLogWriter.record("PUBLISH_REPORT", "DEIReport", report.getId());
        return mapper.toResponse(report);
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        DEIReport report = loadReportOrThrow(id);
        checkReportAccess(report);
        ReportStatus statusAtDeletion = report.getStatus();

        report.getMetrics().clear();
        reportRepository.saveAndFlush(report);

        reportRepository.delete(report);
        reportRepository.flush();
        auditLogWriter.record("DELETE_REPORT", "DEIReport", id);
        log.info("Deleted DEI report id={} (status at deletion={})", id, statusAtDeletion);
    }

    @Override
    @Transactional(readOnly = true)
    public DEIReportResponse getReportById(Long id) {
        DEIReport report = loadReportOrThrow(id);
        checkReportAccess(report);
        if (report.getStatus() != ReportStatus.PUBLISHED && !SecurityUtils.hasRole(Role.ADMIN)) {
            throw new ResourceNotFoundException("Published DEIReport not found with id: " + id);
        }
        auditLogWriter.record("VIEW_REPORT_DEFINITION", "DEIReport", report.getId());
        return mapper.toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DEIReportResponse> listReports(ReportStatus status, Pageable pageable) {
        SecurityUtils.requireCurrentPrincipal();
        auditLogWriter.record("VIEW_REPORT_DEFINITIONS", "DEIReport", null);

        ReportStatus targetStatus = status;
        if (!SecurityUtils.hasRole(Role.ADMIN)) {
            targetStatus = ReportStatus.PUBLISHED;
        }

        if (targetStatus != null) {
            return reportRepository.findByStatus(targetStatus, pageable).map(mapper::toResponse);
        }
        return reportRepository.findAll(pageable).map(mapper::toResponse);
    }

    private void checkReportAccess(DEIReport report) {
        SecurityUtils.requireCurrentPrincipal();
    }

    @Override
    @Transactional(readOnly = true)
    public DEIReportDataResponse computeReportData(Long id) {
        DEIReport report = loadReportOrThrow(id);

        if (report.getStatus() != ReportStatus.PUBLISHED && !SecurityUtils.hasRole(Role.ADMIN)) {
            throw new ResourceNotFoundException("Published DEIReport not found with id: " + id);
        }

        checkReportAccess(report);

        auditLogWriter.record("VIEW_REPORT_DATA", "DEIReport", report.getId());

        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        Long hrId = (report.getCreatedBy() != null && report.getCreatedBy().getRole() == Role.HR_BIZ_PARTNER)
                ? report.getCreatedBy().getId() : null;

        List<RepresentationSnapshotResponse> representationData = null;
        Double inclusionIndex = null;
        Double ergMembershipRate = null;
        Double goalAttainmentRate = null;
        Double payEquityGap = null;

        if (report.getMetrics().contains(ReportMetric.REPRESENTATION_BY_DIMENSION)) {
            representationData = computeRepresentation(report.getScope(), report.getScopeValue(), minGroupSize, hrId);
        }
        if (report.getMetrics().contains(ReportMetric.INCLUSION_INDEX)) {
            inclusionIndex = computeInclusionIndex(report.getScope(), report.getScopeValue(), hrId);
        }
        if (report.getMetrics().contains(ReportMetric.ERG_MEMBERSHIP_RATE)) {
            ergMembershipRate = computeErgMembershipRate(report.getScope(), report.getScopeValue(), hrId);
        }
        if (report.getMetrics().contains(ReportMetric.GOAL_ATTAINMENT_RATE)) {
            goalAttainmentRate = computeGoalAttainmentRate(report.getScope(), report.getScopeValue(), hrId);
        }
        if (report.getMetrics().contains(ReportMetric.PAY_EQUITY_GAP)) {
            payEquityGap = computePayEquityGap(report.getScope(), report.getScopeValue(), minGroupSize, hrId);
        }

        return new DEIReportDataResponse(
                report.getId(),
                report.getScope(),
                report.getScopeValue(),
                report.getGeneratedDate(),
                representationData,
                inclusionIndex,
                ergMembershipRate,
                goalAttainmentRate,
                payEquityGap
        );
    }

    private List<RepresentationSnapshotResponse> computeRepresentation(ReportScope scope, String scopeValue, int minGroupSize, Long hrId) {
        if (hrId != null) {
            return computeRepresentationForHr(scope, scopeValue, minGroupSize, hrId);
        }
        Long departmentId = departmentScopeId(scope, scopeValue);

        Page<RepresentationSnapshot> snapshots = snapshotRepository.search(
                null, departmentId, SnapshotStatus.PUBLISHED, null, PageRequest.of(0, 1000));

        return latestPerGroup(snapshots.getContent()).stream()
                .map(s -> snapshotMapper.toResponse(s, minGroupSize))
                .toList();
    }

    private Long departmentScopeId(ReportScope scope, String scopeValue) {
        if (scope != ReportScope.DEPARTMENT || scopeValue == null) {
            return null;
        }
        try {
            return Long.parseLong(scopeValue);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<RepresentationSnapshot> latestPerGroup(List<RepresentationSnapshot> snapshots) {
        Map<String, RepresentationSnapshot> latest = new LinkedHashMap<>();
        for (RepresentationSnapshot s : snapshots) {
            String key = s.getDimension() + "|" + s.getGroupName() + "|" + s.getDepartmentId();
            RepresentationSnapshot held = latest.get(key);
            if (held == null || s.getSnapshotDate().isAfter(held.getSnapshotDate())) {
                latest.put(key, s);
            }
        }
        return latest.values().stream()
                .sorted(Comparator
                        .comparing((RepresentationSnapshot s) -> s.getDimension().name())
                        .thenComparing(RepresentationSnapshot::getGroupName))
                .toList();
    }

    private List<RepresentationSnapshotResponse> computeRepresentationForHr(
            ReportScope scope, String scopeValue, int minGroupSize, Long hrId) {
        Long departmentId = departmentScopeId(scope, scopeValue);

        List<RepresentationSnapshotResponse> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        addDynamicSnapshots(results, demographicProfileRepository.aggregateByGenderForHr(departmentId, hrId),
                DemographicDimension.GENDER, departmentId, today, minGroupSize);
        addDynamicSnapshots(results, demographicProfileRepository.aggregateByEthnicityForHr(departmentId, hrId),
                DemographicDimension.ETHNICITY, departmentId, today, minGroupSize);
        addDynamicSnapshots(results, demographicProfileRepository.aggregateByDisabilityForHr(departmentId, hrId),
                DemographicDimension.DISABILITY, departmentId, today, minGroupSize);
        addDynamicSnapshots(results, demographicProfileRepository.aggregateByVeteranForHr(departmentId, hrId),
                DemographicDimension.VETERAN, departmentId, today, minGroupSize);
        addDynamicSnapshots(results, demographicProfileRepository.aggregateByAgeGroupForHr(departmentId, hrId),
                DemographicDimension.AGE_GROUP, departmentId, today, minGroupSize);

        return results;
    }

    private void addDynamicSnapshots(List<RepresentationSnapshotResponse> results, List<Object[]> groups,
                                     DemographicDimension dimension, Long departmentId, LocalDate today, int minGroupSize) {
        long total = 0;
        for (Object[] row : groups) {
            if (row[1] != null) {
                total += ((Number) row[1]).longValue();
            }
        }

        for (Object[] row : groups) {
            String groupName = row[0] != null ? row[0].toString() : "Unknown";
            long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
            double percentage = total == 0 ? 0.0 : round((count * 100.0) / total);

            boolean suppressed = count < minGroupSize;

            results.add(new RepresentationSnapshotResponse(
                    null,
                    today,
                    departmentId,
                    DepartmentName.fromId(departmentId),
                    dimension,
                    groupName,
                    suppressed ? null : (int) count,
                    suppressed ? null : percentage,
                    SnapshotStatus.PUBLISHED,
                    suppressed
            ));
        }
    }

    private Double computeInclusionIndex(ReportScope scope, String scopeValue, Long hrId) {
        try {
            Double avg;
            if (hrId != null) {
                avg = surveyClient.getAverageInclusionIndex("HR", String.valueOf(hrId));
            } else if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                avg = surveyClient.getAverageInclusionIndex("DEPARTMENT", scopeValue);
            } else if (scope == ReportScope.GRADE && scopeValue != null) {
                avg = surveyClient.getAverageInclusionIndex("GRADE", scopeValue);
            } else {
                avg = surveyClient.getAverageInclusionIndex(null, null);
            }
            return avg != null ? round(avg) : 0.0;
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private Double computeErgMembershipRate(ReportScope scope, String scopeValue, Long hrId) {
      try {
        long activeUsers = 0;
        long distinctActiveMembers = 0;

        if (hrId != null) {
            if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                try {
                    Long deptId = Long.parseLong(scopeValue);
                    activeUsers = userRepository.countByStatusAndHr_IdAndDepartmentId(UserStatus.ACTIVE, hrId, deptId);
                    distinctActiveMembers = ergClient.getActiveMemberCount("DEPARTMENT", String.valueOf(deptId), hrId);
                } catch (NumberFormatException ignored) {}
            } else if (scope == ReportScope.GRADE && scopeValue != null) {
                try {
                    Long grId = Long.parseLong(scopeValue);
                    activeUsers = userRepository.countByStatusAndHr_IdAndGradeId(UserStatus.ACTIVE, hrId, grId);
                    distinctActiveMembers = ergClient.getActiveMemberCount("GRADE", String.valueOf(grId), hrId);
                } catch (NumberFormatException ignored) {}
            } else {
                activeUsers = userRepository.countByStatusAndHr_Id(UserStatus.ACTIVE, hrId);
                distinctActiveMembers = ergClient.getActiveMemberCount("ALL", null, hrId);
            }
        } else {
            if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                try {
                    Long deptId = Long.parseLong(scopeValue);
                    activeUsers = userRepository.countByStatusAndDepartmentId(UserStatus.ACTIVE, deptId);
                    distinctActiveMembers = ergClient.getActiveMemberCount("DEPARTMENT", String.valueOf(deptId), null);
                } catch (NumberFormatException ignored) {}
            } else if (scope == ReportScope.GRADE && scopeValue != null) {
                try {
                    Long grId = Long.parseLong(scopeValue);
                    activeUsers = userRepository.countByStatusAndGradeId(UserStatus.ACTIVE, grId);
                    distinctActiveMembers = ergClient.getActiveMemberCount("GRADE", String.valueOf(grId), null);
                } catch (NumberFormatException ignored) {}
            } else {
                activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
                distinctActiveMembers = ergClient.getActiveMemberCount("ALL", null, null);
            }
        }

        if (activeUsers == 0) {
            return 0.0;
        }
        return round((distinctActiveMembers * 100.0) / activeUsers);
      } catch (Exception ex) {
        return 0.0;
      }
    }

    private Double computeGoalAttainmentRate(ReportScope scope, String scopeValue, Long hrId) {
        long achievedGoals = 0;
        long totalGoals = 0;

        if (hrId != null) {
            if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                try {
                    Long deptId = Long.parseLong(scopeValue);
                    achievedGoals = goalRepository.countByStatusAndOwner_HrIdAndOwner_DepartmentId(GoalStatus.ACHIEVED, hrId, deptId);
                    totalGoals = goalRepository.countByOwner_HrIdAndOwner_DepartmentId(hrId, deptId);
                } catch (NumberFormatException ignored) {}
            } else if (scope == ReportScope.GRADE && scopeValue != null) {
                try {
                    Long grId = Long.parseLong(scopeValue);
                    achievedGoals = goalRepository.countByStatusAndOwner_HrIdAndOwner_GradeId(GoalStatus.ACHIEVED, hrId, grId);
                    totalGoals = goalRepository.countByOwner_HrIdAndOwner_GradeId(hrId, grId);
                } catch (NumberFormatException ignored) {}
            } else {
                achievedGoals = goalRepository.countByStatusAndOwner_HrId(GoalStatus.ACHIEVED, hrId);
                totalGoals = goalRepository.countByOwner_HrId(hrId);
            }
        } else {
            if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                try {
                    Long deptId = Long.parseLong(scopeValue);
                    achievedGoals = goalRepository.countByStatusAndOwner_DepartmentId(GoalStatus.ACHIEVED, deptId);
                    totalGoals = goalRepository.countByOwner_DepartmentId(deptId);
                } catch (NumberFormatException ignored) {}
            } else if (scope == ReportScope.GRADE && scopeValue != null) {
                try {
                    Long grId = Long.parseLong(scopeValue);
                    achievedGoals = goalRepository.countByStatusAndOwner_GradeId(GoalStatus.ACHIEVED, grId);
                    totalGoals = goalRepository.countByOwner_GradeId(grId);
                } catch (NumberFormatException ignored) {}
            } else {
                achievedGoals = goalRepository.countByStatus(GoalStatus.ACHIEVED);
                totalGoals = goalRepository.count();
            }
        }

        if (totalGoals == 0) {
            return 0.0;
        }
        return round((achievedGoals * 100.0) / totalGoals);
    }

    private Double computePayEquityGap(ReportScope scope, String scopeValue, int minGroupSize, Long hrId) {
        List<PayEquityAnalysis> publishedAnalyses = payAnalysisRepository.search(
                null, AnalysisStatus.PUBLISHED, hrId, Pageable.unpaged()).getContent();

        List<Double> validGaps = new ArrayList<>();

        for (PayEquityAnalysis analysis : publishedAnalyses) {
            List<PayGapFlag> flags = payFlagRepository.findByAnalysisId(analysis.getId());
            for (PayGapFlag flag : flags) {
                if (scope == ReportScope.DEPARTMENT && scopeValue != null) {
                    try {
                        Long deptId = Long.parseLong(scopeValue);
                        if (!deptId.equals(flag.getDepartmentId())) {
                            continue;
                        }
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                } else if (scope == ReportScope.GRADE && scopeValue != null) {
                    try {
                        Long grId = Long.parseLong(scopeValue);
                        if (!grId.equals(flag.getGradeId())) {
                            continue;
                        }
                    } catch (NumberFormatException ignored) {
                        continue;
                    }
                }

                if (flag.getAffectedEmployeeCount() != null && flag.getAffectedEmployeeCount() >= minGroupSize) {
                    if (flag.getGapPercent() != null) {
                        validGaps.add(flag.getGapPercent());
                    }
                }
            }
        }

        if (validGaps.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (double gap : validGaps) {
            sum += gap;
        }
        return round(sum / validGaps.size());
    }

    private DEIReport loadReportOrThrow(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DEIReport", id));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
