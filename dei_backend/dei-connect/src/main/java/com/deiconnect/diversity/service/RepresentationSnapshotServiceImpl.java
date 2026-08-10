package com.deiconnect.diversity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.PrivacyThresholdViolationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.diversity.dto.GenerateSnapshotRequest;
import com.deiconnect.diversity.dto.GenerateSnapshotResult;
import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.dto.SnapshotGroupResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentationSnapshotServiceImpl implements RepresentationSnapshotService {

    private static final Set<Role> PUBLISHED_ONLY_ROLES = EnumSet.of(Role.EXECUTIVE, Role.ADMIN);

    private final RepresentationSnapshotRepository snapshotRepository;
    private final DemographicProfileRepository profileRepository;
    private final RepresentationSnapshotMapper snapshotMapper;
    private final PrivacyProperties privacyProperties;
    private final AuditLogWriter auditLogWriter;
    private final UserRepository userRepository;
    private final NotificationEmitter notificationEmitter;

    @Override
    @Transactional
    public GenerateSnapshotResult generate(GenerateSnapshotRequest payload) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        Long creatorManagerId = SecurityUtils.getCurrentUserId();
        Long aggregationManagerId = SecurityUtils.getCurrentRole() == Role.DEI_MANAGER
                ? creatorManagerId
                : null;

        List<RepresentationSnapshot> previousRun = snapshotRepository.findDistribution(
                payload.snapshotDate(), payload.dimension(), payload.departmentId(), creatorManagerId);
        if (!previousRun.isEmpty()) {
            boolean anyPublished = previousRun.stream()
                    .anyMatch(snapshot -> snapshot.getStatus() == SnapshotStatus.PUBLISHED);
            if (anyPublished) {
                throw new ConflictException("A published " + payload.dimension()
                        + " snapshot already exists for " + payload.snapshotDate()
                        + ". Delete it first if you need to regenerate.");
            }
            snapshotRepository.deleteAll(previousRun);
            snapshotRepository.flush();
        }

        List<Object[]> aggregatedRows = aggregate(payload.dimension(), payload.departmentId(), aggregationManagerId);
        long totalConsentedCount = aggregatedRows.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();

        int suppressedGroupCount = (int) aggregatedRows.stream()
                .filter(row -> ((Number) row[1]).longValue() < minGroupSize)
                .count();

        List<RepresentationSnapshot> snapshotsToSave = new ArrayList<>();
        for (Object[] row : aggregatedRows) {
            String groupName = row[0] == null ? "Unknown" : String.valueOf(row[0]);
            long groupCount = ((Number) row[1]).longValue();

            if (groupCount < minGroupSize) {
                continue;
            }

            double percentage = totalConsentedCount == 0 ? 0.0 : round((groupCount * 100.0) / totalConsentedCount);
            snapshotsToSave.add(RepresentationSnapshot.builder()
                    .snapshotDate(payload.snapshotDate())
                    .departmentId(payload.departmentId())
                    .dimension(payload.dimension())
                    .groupName(groupName)
                    .count((int) groupCount)
                    .percentage(percentage)
                    .status(SnapshotStatus.DRAFT)
                    .creatorManagerId(creatorManagerId)
                    .suppressedGroupCount(suppressedGroupCount)
                    .totalConsidered((int) totalConsentedCount)
                    .build());
        }

        List<RepresentationSnapshotResponse> producedSnapshots = snapshotRepository.saveAll(snapshotsToSave).stream()
                .map(savedSnapshot -> snapshotMapper.toResponse(savedSnapshot, minGroupSize))
                .toList();

        auditLogWriter.record("GENERATE_SNAPSHOT", "RepresentationSnapshot", null);
        return new GenerateSnapshotResult(payload.dimension(), totalConsentedCount, producedSnapshots, suppressedGroupCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RepresentationSnapshotResponse> search(DemographicDimension dimension,
                                                       Long departmentId,
                                                       SnapshotStatus status,
                                                       Pageable pageable) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        auditLogWriter.record("VIEW_SNAPSHOTS", "RepresentationSnapshot", null);
        return snapshotRepository
                .search(dimension, departmentId, effectiveStatus(status), readScopeManagerId(), pageable)
                .map(snapshot -> snapshotMapper.toResponse(snapshot, minGroupSize));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnapshotRunResponse> searchRuns(DemographicDimension dimension,
                                                Long departmentId,
                                                SnapshotStatus status,
                                                Pageable pageable) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        auditLogWriter.record("VIEW_SNAPSHOTS", "RepresentationSnapshot", null);

        SnapshotStatus effectiveFilterStatus = effectiveStatus(status);
        Long scopedManagerId = readScopeManagerId();

        List<RepresentationSnapshot> allSnapshots = snapshotRepository.findAllForRuns(
                dimension, departmentId, effectiveFilterStatus, scopedManagerId);

        Map<String, List<RepresentationSnapshot>> snapshotsByRun = new LinkedHashMap<>();
        for (RepresentationSnapshot snapshot : allSnapshots) {
            snapshotsByRun.computeIfAbsent(runKey(snapshot), k -> new ArrayList<>()).add(snapshot);
        }

        List<SnapshotRunResponse> runResponses = snapshotsByRun.values().stream()
                .map(group -> toRun(group, minGroupSize))
                .toList();

        int fromIndex = (int) Math.min(pageable.getOffset(), runResponses.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), runResponses.size());

        List<SnapshotRunResponse> pageSlice = runResponses.subList(fromIndex, toIndex);

        return new PageImpl<>(pageSlice, pageable, runResponses.size());
    }

    private static String runKey(RepresentationSnapshot snapshot) {
        return snapshot.getSnapshotDate() + "|" + snapshot.getDimension() + "|" + snapshot.getDepartmentId()
                + "|" + snapshot.getCreatorManagerId();
    }

    private SnapshotRunResponse toRun(List<RepresentationSnapshot> snapshotGroup, int minGroupSize) {
        RepresentationSnapshot firstSnapshot = snapshotGroup.get(0);
        List<RepresentationSnapshotResponse> groupResponses = snapshotGroup.stream()
                .map(snapshot -> snapshotMapper.toResponse(snapshot, minGroupSize))
                .toList();

        int totalHeadCount = visibleHeadCount(groupResponses);
        boolean allPublished = snapshotGroup.stream().allMatch(snapshot -> snapshot.getStatus() == SnapshotStatus.PUBLISHED);

        return new SnapshotRunResponse(
                firstSnapshot.getId(),
                firstSnapshot.getSnapshotDate(),
                firstSnapshot.getDimension(),
                firstSnapshot.getDepartmentId(),
                DepartmentName.fromId(firstSnapshot.getDepartmentId()),
                allPublished ? SnapshotStatus.PUBLISHED : SnapshotStatus.DRAFT,
                groupResponses.size(),
                suppressedCountOf(firstSnapshot, groupResponses),
                totalHeadCount,
                totalConsideredOf(firstSnapshot, totalHeadCount),
                groupResponses);
    }

    private int suppressedCountOf(RepresentationSnapshot anchorSnapshot, List<RepresentationSnapshotResponse> groupResponses) {
        Integer recordedCount = anchorSnapshot.getSuppressedGroupCount();
        if (recordedCount != null) {
            return recordedCount;
        }
        return (int) groupResponses.stream().filter(RepresentationSnapshotResponse::suppressed).count();
    }

    private int totalConsideredOf(RepresentationSnapshot anchorSnapshot, int visibleHeadCount) {
        Integer recordedCount = anchorSnapshot.getTotalConsidered();
        return recordedCount != null ? recordedCount : visibleHeadCount;
    }

    private static int visibleHeadCount(List<RepresentationSnapshotResponse> groupResponses) {
        return groupResponses.stream()
                .filter(group -> group.count() != null)
                .mapToInt(RepresentationSnapshotResponse::count)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public RepresentationSnapshotResponse getById(Long snapshotId) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        RepresentationSnapshot snapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(snapshot);
        requireVisible(snapshot);
        auditLogWriter.record("VIEW_SNAPSHOT", "RepresentationSnapshot", snapshot.getId());
        return snapshotMapper.toResponse(snapshot, minGroupSize);
    }

    @Override
    @Transactional(readOnly = true)
    public SnapshotGroupResponse getDistribution(Long snapshotId) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        RepresentationSnapshot snapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(snapshot);
        requireVisible(snapshot);

        List<RepresentationSnapshot> distribution = runOf(snapshot);
        List<RepresentationSnapshotResponse> groupResponses = distribution.stream()
                .map(s -> snapshotMapper.toResponse(s, minGroupSize))
                .toList();

        int totalHeadCount = visibleHeadCount(groupResponses);

        auditLogWriter.record("VIEW_SNAPSHOT_DISTRIBUTION", "RepresentationSnapshot", snapshot.getId());

        return new SnapshotGroupResponse(
                snapshot.getSnapshotDate(),
                snapshot.getDimension(),
                snapshot.getDepartmentId(),
                DepartmentName.fromId(snapshot.getDepartmentId()),
                totalHeadCount,
                groupResponses.size(),
                suppressedCountOf(snapshot, groupResponses),
                totalConsideredOf(snapshot, totalHeadCount),
                groupResponses);
    }

    @Override
    @Transactional
    public RepresentationSnapshotResponse publish(Long snapshotId) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        RepresentationSnapshot snapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(snapshot);
        requireVisible(snapshot);

        if (snapshot.getCount() == null || snapshot.getCount() < minGroupSize) {
            throw new PrivacyThresholdViolationException(
                    "Cannot publish a snapshot whose group size is below the minimum threshold");
        }
        boolean wasAlreadyPublished = snapshot.getStatus() == SnapshotStatus.PUBLISHED;
        snapshot.setStatus(SnapshotStatus.PUBLISHED);
        snapshot = snapshotRepository.save(snapshot);
        auditLogWriter.record("PUBLISH_SNAPSHOT", "RepresentationSnapshot", snapshot.getId());

        if (!wasAlreadyPublished) {
            notifyExecutivesOfPublication(snapshot);
        }
        return snapshotMapper.toResponse(snapshot, minGroupSize);
    }

    @Override
    @Transactional
    public SnapshotRunResponse publishRun(Long snapshotId) {
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        RepresentationSnapshot anchorSnapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(anchorSnapshot);

        List<RepresentationSnapshot> runSnapshots = runOf(anchorSnapshot);
        List<RepresentationSnapshot> publishableSnapshots = runSnapshots.stream()
                .filter(snapshot -> snapshot.getCount() != null && snapshot.getCount() >= minGroupSize)
                .toList();
        if (publishableSnapshots.isEmpty()) {
            throw new PrivacyThresholdViolationException(
                    "Cannot publish this snapshot: every group is below the minimum group size");
        }

        boolean wasAlreadyPublished = runSnapshots.stream()
                .allMatch(snapshot -> snapshot.getStatus() == SnapshotStatus.PUBLISHED);

        publishableSnapshots.forEach(snapshot -> snapshot.setStatus(SnapshotStatus.PUBLISHED));
        snapshotRepository.saveAll(publishableSnapshots);
        snapshotRepository.flush();

        auditLogWriter.record("PUBLISH_SNAPSHOT", "RepresentationSnapshot", anchorSnapshot.getId());

        if (!wasAlreadyPublished) {
            notifyExecutivesOfPublication(anchorSnapshot);
        }
        return toRun(runOf(anchorSnapshot), minGroupSize);
    }

    @Override
    @Transactional
    public void delete(Long snapshotId) {
        RepresentationSnapshot snapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(snapshot);
        requireVisible(snapshot);
        snapshotRepository.delete(snapshot);
        auditLogWriter.record("DELETE_SNAPSHOT", "RepresentationSnapshot", snapshotId);
    }

    @Override
    @Transactional
    public void deleteRun(Long snapshotId) {
        RepresentationSnapshot anchorSnapshot = findOrThrow(snapshotId);
        requireOwnershipForManager(anchorSnapshot);
        List<RepresentationSnapshot> runSnapshots = runOf(anchorSnapshot);
        snapshotRepository.deleteAll(runSnapshots);
        auditLogWriter.record("DELETE_SNAPSHOT", "RepresentationSnapshot", snapshotId);
    }

    private List<RepresentationSnapshot> runOf(RepresentationSnapshot snapshot) {
        List<RepresentationSnapshot> runSnapshots = snapshotRepository.findDistribution(
                snapshot.getSnapshotDate(),
                snapshot.getDimension(),
                snapshot.getDepartmentId(),
                snapshot.getCreatorManagerId());
        return runSnapshots.isEmpty() ? List.of(snapshot) : runSnapshots;
    }

    private Long readScopeManagerId() {
        return SecurityUtils.getCurrentRole() == Role.DEI_MANAGER ? SecurityUtils.getCurrentUserId() : null;
    }

    private SnapshotStatus effectiveStatus(SnapshotStatus requestedStatus) {
        return seesPublishedOnly() ? SnapshotStatus.PUBLISHED : requestedStatus;
    }

    private void requireVisible(RepresentationSnapshot snapshot) {
        if (seesPublishedOnly() && snapshot.getStatus() != SnapshotStatus.PUBLISHED) {
            throw new ResourceNotFoundException("RepresentationSnapshot", snapshot.getId());
        }
    }

    private boolean seesPublishedOnly() {
        return PUBLISHED_ONLY_ROLES.contains(SecurityUtils.getCurrentRole());
    }

    private void notifyExecutivesOfPublication(RepresentationSnapshot anchorSnapshot) {
        try {
            String scope = anchorSnapshot.getDepartmentId() == null
                    ? "organisation-wide"
                    : String.valueOf(DepartmentName.fromId(anchorSnapshot.getDepartmentId()));
            String message = "Representation snapshot published: " + anchorSnapshot.getDimension()
                    + " (" + scope + ") for " + anchorSnapshot.getSnapshotDate();

            Page<User> executivePage = userRepository.findByRole(Role.EXECUTIVE, PageRequest.of(0, 1000));
            for (User executiveUser : executivePage.getContent()) {
                if (executiveUser.getStatus() == UserStatus.ACTIVE && executiveUser.getEmployeeId() != null) {
                    notificationEmitter.emit(executiveUser.getEmployeeId(),
                            NotificationCategory.REPORT, message);
                }
            }
        } catch (Exception ex) {
            log.error("Snapshot {} published, but notifying executives failed: {}",
                    anchorSnapshot.getId(), ex.getMessage(), ex);
        }
    }

    private List<Object[]> aggregate(DemographicDimension dimension, Long departmentId, Long managerId) {
        if (managerId != null) {
            return switch (dimension) {
                case GENDER -> profileRepository.aggregateByGenderForManager(departmentId, managerId);
                case ETHNICITY -> profileRepository.aggregateByEthnicityForManager(departmentId, managerId);
                case DISABILITY -> profileRepository.aggregateByDisabilityForManager(departmentId, managerId);
                case VETERAN -> profileRepository.aggregateByVeteranForManager(departmentId, managerId);
                case AGE_GROUP -> profileRepository.aggregateByAgeGroupForManager(departmentId, managerId);
            };
        }
        return switch (dimension) {
            case GENDER -> profileRepository.aggregateByGender(departmentId);
            case ETHNICITY -> profileRepository.aggregateByEthnicity(departmentId);
            case DISABILITY -> profileRepository.aggregateByDisability(departmentId);
            case VETERAN -> profileRepository.aggregateByVeteran(departmentId);
            case AGE_GROUP -> profileRepository.aggregateByAgeGroup(departmentId);
        };
    }

    private RepresentationSnapshot findOrThrow(Long snapshotId) {
        return snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("RepresentationSnapshot", snapshotId));
    }

    private void requireOwnershipForManager(RepresentationSnapshot snapshot) {
        if (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (snapshot.getCreatorManagerId() == null || !snapshot.getCreatorManagerId().equals(currentUserId)) {
                throw new ForbiddenOperationException("You may only access snapshots you generated");
            }
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}