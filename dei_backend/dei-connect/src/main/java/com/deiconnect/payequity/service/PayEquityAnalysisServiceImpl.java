package com.deiconnect.payequity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.config.PrivacyProperties;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.notification.enums.NotificationCategory;
import com.deiconnect.notification.service.NotificationEmitter;
import com.deiconnect.payequity.dto.PayEquityAnalysisRequest;
import com.deiconnect.payequity.dto.PayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PayGapFlagResponse;
import com.deiconnect.payequity.dto.PublishedPayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PublishedPayGapFlagResponse;
import com.deiconnect.payequity.dto.UpdatePayGapFlagRequest;
import com.deiconnect.payequity.entity.PayEquityAnalysis;
import com.deiconnect.payequity.entity.PayGapFlag;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.FlagStatus;
import com.deiconnect.payequity.enums.PayDimension;
import com.deiconnect.payequity.mapper.PayEquityMapper;
import com.deiconnect.payequity.repository.PayEquityAnalysisRepository;
import com.deiconnect.payequity.repository.PayGapFlagRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayEquityAnalysisServiceImpl implements PayEquityAnalysisService {

    private final PayEquityAnalysisRepository analysisRepository;
    private final PayGapFlagRepository flagRepository;
    private final UserRepository userRepository;
    private final PayEquityMapper mapper;
    private final PrivacyProperties privacyProperties;
    private final AuditLogWriter auditLogWriter;
    private final NotificationEmitter notificationEmitter;
    private final DemographicProfileRepository demographicProfileRepository;

    @Override
    @Transactional
    public PayEquityAnalysisResponse createAnalysis(PayEquityAnalysisRequest request) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();

        PayEquityAnalysis analysis = PayEquityAnalysis.builder()
                .analysisPeriod(request.analysisPeriod())
                .dimension(request.dimension())
                .controlVariables(request.controlVariables())
                .medianGapPercent(0.0)
                .adjustedGapPercent(0.0)
                .significanceLevel(1.0)
                .runBy(userRepository.getReferenceById(principal.getId()))
                .status(AnalysisStatus.DRAFT)
                .build();

        analysis = analysisRepository.save(analysis);
        auditLogWriter.record("RUN_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toResponse(analysis);
    }

    @Override
    @Transactional
    public PayEquityAnalysisResponse updateAnalysis(Long id, PayEquityAnalysisRequest request) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(id);
        checkHRAccess(analysis);
        if (analysis.getStatus() != AnalysisStatus.DRAFT) {
            throw new ConflictException("Cannot update an analysis that is already published");
        }

        analysis.setAnalysisPeriod(request.analysisPeriod());
        analysis.setDimension(request.dimension());
        analysis.setControlVariables(request.controlVariables());

        analysis = analysisRepository.save(analysis);
        auditLogWriter.record("UPDATE_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toResponse(analysis);
    }

    @Override
    @Transactional
    public PayEquityAnalysisResponse publishAnalysis(Long id) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(id);
        checkHRAccess(analysis);
        if (analysis.getStatus() == AnalysisStatus.PUBLISHED) {
            throw new ConflictException("Analysis is already published");
        }

        analysis.setStatus(AnalysisStatus.PUBLISHED);
        analysis = analysisRepository.save(analysis);

        String message = "Pay equity analysis for " + analysis.getAnalysisPeriod() + " has been published.";
        for (Role role : List.of(Role.DEI_MANAGER, Role.HR_BIZ_PARTNER, Role.EXECUTIVE, Role.ADMIN)) {
            Page<User> usersPage = userRepository.findByRole(role, PageRequest.of(0, 1000));
            for (User u : usersPage.getContent()) {
                if (u.getStatus() == UserStatus.ACTIVE) {
                    notificationEmitter.emit(u.getEmployeeId(), NotificationCategory.PAY_EQUITY, message);
                }
            }
        }

        auditLogWriter.record("PUBLISH_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public PayEquityAnalysisResponse getAnalysisById(Long id) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(id);
        checkHRAccess(analysis);
        auditLogWriter.record("VIEW_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayEquityAnalysisResponse> listAnalyses(PayDimension dimension, AnalysisStatus status, Long hrIdFilter, Pageable pageable) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        Long runById;
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            runById = principal.getId();
        } else if (principal.getRole() == Role.ADMIN && hrIdFilter != null) {
            runById = hrIdFilter;
        } else {
            runById = null;
        }

        auditLogWriter.record("VIEW_ANALYSES", "PayEquityAnalysis", null);
        return analysisRepository.search(dimension, status, runById, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public PayGapFlagResponse updateFlag(Long analysisId, Long flagId, UpdatePayGapFlagRequest request) {
        PayGapFlag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("PayGapFlag", flagId));

        if (!flag.getAnalysis().getId().equals(analysisId)) {
            throw new ConflictException("Flag does not belong to the specified analysis");
        }

        checkHRAccess(flag.getAnalysis());

        User owner = userRepository.findById(request.remediationOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.remediationOwnerId()));

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            if (owner.getRole() == Role.EMPLOYEE) {
                if (owner.getHr() == null || !owner.getHr().getId().equals(principal.getId())) {
                    throw new ForbiddenOperationException("You may only assign employees assigned to you as remediation owners");
                }
            }
        }

        flag.setRemediationOwner(owner);
        flag.setStatus(request.status());
        flag = flagRepository.save(flag);

        auditLogWriter.record("REMEDIATE_PAY_GAP_FLAG", "PayGapFlag", flag.getId());
        return mapper.toResponse(flag);
    }

    @Override
    @Transactional
    public PayEquityAnalysisResponse computeFromWorkforce(Long id) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(id);
        checkHRAccess(analysis);
        if (analysis.getStatus() != AnalysisStatus.DRAFT) {
            throw new ConflictException("Only a DRAFT analysis can be computed");
        }

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        boolean byGender = analysis.getDimension() == PayDimension.GENDER;

        List<Object[]> rows;
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            Long hrId = principal.getId();
            rows = byGender ? demographicProfileRepository.genderSalaryRowsForHr(hrId)
                            : demographicProfileRepository.ethnicitySalaryRowsForHr(hrId);
        } else {
            rows = byGender ? demographicProfileRepository.genderSalaryRows()
                            : demographicProfileRepository.ethnicitySalaryRows();
        }

        Map<String, List<double[]>> groups = new HashMap<>();
        for (Object[] r : rows) {
            String dim = r[0] == null ? null : r[0].toString();
            BigDecimal sal = (BigDecimal) r[1];
            if (dim == null || dim.isBlank() || sal == null) {
                continue;
            }
            Integer exp = (Integer) r[2];
            double e = (exp == null) ? Double.NaN : exp.doubleValue();
            groups.computeIfAbsent(dim, k -> new ArrayList<>()).add(new double[]{sal.doubleValue(), e});
        }

        analysis.getFlags().clear();

        Map<String, Double> medianByGroup = new HashMap<>();
        String refGroup = null;
        double refMedian = -1.0;
        for (Map.Entry<String, List<double[]>> e : groups.entrySet()) {
            double m = medianOf(salaries(e.getValue()));
            medianByGroup.put(e.getKey(), m);
            if (m > refMedian) {
                refMedian = m;
                refGroup = e.getKey();
            }
        }

        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        List<Double> gaps = new ArrayList<>();
        if (refGroup != null && refMedian > 0) {
            for (Map.Entry<String, List<double[]>> e : groups.entrySet()) {
                if (e.getKey().equals(refGroup)) {
                    continue;
                }
                int size = e.getValue().size();
                if (size < minGroupSize) {
                    continue;
                }
                double rawGap = round((refMedian - medianByGroup.get(e.getKey())) / refMedian * 100.0);
                PayGapFlag flag = PayGapFlag.builder()
                        .analysis(analysis)
                        .groupName(e.getKey())
                        .gapPercent(rawGap)
                        .affectedEmployeeCount(size)
                        .status(FlagStatus.OPEN)
                        .build();
                analysis.getFlags().add(flag);
                gaps.add(rawGap);
            }
        }

        double median = gaps.isEmpty() ? 0.0 : medianOf(gaps);
        double adjusted = (refGroup == null) ? 0.0 : Math.max(0.0, experienceAdjustedGap(groups, refGroup));
        double significance = significanceFor(median);

        analysis.setMedianGapPercent(round(median));
        analysis.setAdjustedGapPercent(round(adjusted));
        analysis.setSignificanceLevel(significance);
        analysisRepository.save(analysis);

        auditLogWriter.record("COMPUTE_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayGapFlagResponse> listFlags(Long analysisId) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(analysisId);
        checkHRAccess(analysis);
        auditLogWriter.record("VIEW_PAY_GAP_FLAGS", "PayGapFlag", null);
        return flagRepository.findByAnalysisId(analysisId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void checkHRAccess(PayEquityAnalysis analysis) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            if (analysis.getRunBy() == null || !analysis.getRunBy().getId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You may only access your own pay equity analyses");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublishedPayEquityAnalysisResponse> listPublishedAnalyses(Pageable pageable) {
        auditLogWriter.record("VIEW_PUBLISHED_ANALYSES", "PayEquityAnalysis", null);
        return analysisRepository.search(null, AnalysisStatus.PUBLISHED, null, pageable)
                .map(mapper::toPublishedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PublishedPayEquityAnalysisResponse getPublishedAnalysisById(Long id) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(id);
        if (analysis.getStatus() != AnalysisStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published PayEquityAnalysis not found with id: " + id);
        }
        auditLogWriter.record("VIEW_PUBLISHED_ANALYSIS", "PayEquityAnalysis", analysis.getId());
        return mapper.toPublishedResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublishedPayGapFlagResponse> listPublishedFlags(Long analysisId) {
        PayEquityAnalysis analysis = loadAnalysisOrThrow(analysisId);
        if (analysis.getStatus() != AnalysisStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published PayEquityAnalysis not found with id: " + analysisId);
        }
        int minGroupSize = privacyProperties.getDefaultMinGroupSize();
        auditLogWriter.record("VIEW_PUBLISHED_FLAGS", "PayGapFlag", null);
        return flagRepository.findByAnalysisId(analysisId).stream()
                .map(flag -> mapper.toPublishedResponse(flag, minGroupSize))
                .toList();
    }

    private PayEquityAnalysis loadAnalysisOrThrow(Long id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayEquityAnalysis", id));
    }

    private void recalculateMetrics(PayEquityAnalysis analysis) {
        List<PayGapFlag> flags = flagRepository.findByAnalysisId(analysis.getId());
        if (flags.isEmpty()) {
            analysis.setMedianGapPercent(0.0);
            analysis.setAdjustedGapPercent(0.0);
            analysis.setSignificanceLevel(1.0);
            return;
        }

        List<Double> gaps = flags.stream()
                .map(PayGapFlag::getGapPercent)
                .filter(java.util.Objects::nonNull)
                .sorted()
                .toList();

        if (gaps.isEmpty()) {
            analysis.setMedianGapPercent(0.0);
            analysis.setAdjustedGapPercent(0.0);
            analysis.setSignificanceLevel(1.0);
            return;
        }

        double median;
        int size = gaps.size();
        if (size % 2 == 1) {
            median = gaps.get(size / 2);
        } else {
            median = (gaps.get(size / 2 - 1) + gaps.get(size / 2)) / 2.0;
        }

        median = round(median);

        int controlCount = analysis.getControlVariables() == null ? 0 : analysis.getControlVariables().size();
        double explainedFactor = Math.max(0.0, 1.0 - (0.10 * controlCount));
        double adjusted = round(median * explainedFactor);

        analysis.setMedianGapPercent(median);
        analysis.setAdjustedGapPercent(adjusted);
        analysis.setSignificanceLevel(significanceFor(median));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double significanceFor(double median) {
        if (median >= 10.0) {
            return 0.01;
        }
        if (median >= 5.0) {
            return 0.05;
        }
        return 0.10;
    }

    private static List<Double> salaries(List<double[]> members) {
        List<Double> out = new ArrayList<>();
        for (double[] m : members) {
            out.add(m[0]);
        }
        return out;
    }

    private static double medianOf(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        return (n % 2 == 1) ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }

    private static int band(double experience) {
        if (Double.isNaN(experience)) return -1;
        if (experience <= 2) return 0;
        if (experience <= 5) return 1;
        if (experience <= 10) return 2;
        return 3;
    }

    private static List<Double> salariesInBand(List<double[]> members, int targetBand) {
        List<Double> out = new ArrayList<>();
        for (double[] m : members) {
            if (band(m[1]) == targetBand) {
                out.add(m[0]);
            }
        }
        return out;
    }

    private double experienceAdjustedGap(Map<String, List<double[]>> groups, String refGroup) {
        List<double[]> ref = groups.get(refGroup);
        if (ref == null) {
            return 0.0;
        }
        double weightedSum = 0.0;
        int totalWeight = 0;
        for (Map.Entry<String, List<double[]>> e : groups.entrySet()) {
            if (e.getKey().equals(refGroup)) {
                continue;
            }
            for (int b = 0; b <= 3; b++) {
                List<Double> refBand = salariesInBand(ref, b);
                List<Double> groupBand = salariesInBand(e.getValue(), b);
                if (refBand.isEmpty() || groupBand.isEmpty()) {
                    continue;
                }
                double refMed = medianOf(refBand);
                if (refMed <= 0) {
                    continue;
                }
                double bandGap = (refMed - medianOf(groupBand)) / refMed * 100.0;
                int weight = groupBand.size();
                weightedSum += bandGap * weight;
                totalWeight += weight;
            }
        }
        return totalWeight == 0 ? 0.0 : round(weightedSum / totalWeight);
    }
}
