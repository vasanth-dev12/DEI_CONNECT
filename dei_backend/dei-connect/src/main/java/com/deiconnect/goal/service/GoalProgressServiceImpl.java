package com.deiconnect.goal.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.goal.dto.CreateProgressRequest;
import com.deiconnect.goal.dto.ProgressResponse;
import com.deiconnect.goal.dto.UpdateProgressRequest;
import com.deiconnect.goal.entity.DEIGoal;
import com.deiconnect.goal.entity.GoalProgressEntry;
import com.deiconnect.goal.enums.ProgressStatus;
import com.deiconnect.goal.enums.ProgressTrend;
import com.deiconnect.goal.mapper.GoalProgressMapper;
import com.deiconnect.goal.repository.GoalProgressEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalProgressServiceImpl implements GoalProgressService {

    private final GoalProgressEntryRepository progressRepository;
    private final DEIGoalService goalService;
    private final GoalProgressMapper progressMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public ProgressResponse create(Long goalId, CreateProgressRequest request) {
        DEIGoal goal = goalService.findVisibleOrThrow(goalId);

        Double previousActual = progressRepository.findFirstByGoal_IdOrderByIdDesc(goalId)
                .map(GoalProgressEntry::getActualValue)
                .orElse(null);

        GoalProgressEntry entry = GoalProgressEntry.builder()
                .goal(goal)
                .period(request.period())
                .actualValue(request.actualValue())
                .gapToTarget(round(goal.getTargetValue() - request.actualValue()))
                .trend(computeTrend(goal, previousActual, request.actualValue()))
                .commentary(request.commentary())
                .status(ProgressStatus.DRAFT)
                .build();

        entry = progressRepository.save(entry);
        auditLogWriter.record("CREATE_PROGRESS", "GoalProgressEntry", entry.getId());
        return progressMapper.toResponse(entry);
    }

    @Override
    @Transactional
    public ProgressResponse update(Long goalId, Long progressId, UpdateProgressRequest request) {
        goalService.findVisibleOrThrow(goalId);
        GoalProgressEntry entry = loadInGoal(goalId, progressId);
        if (entry.getStatus() == ProgressStatus.CONFIRMED) {
            throw new ConflictException("Confirmed progress entries are immutable");
        }
        DEIGoal goal = entry.getGoal();

        Double previousActual = progressRepository
                .findFirstByGoal_IdAndIdLessThanOrderByIdDesc(goalId, entry.getId())
                .map(GoalProgressEntry::getActualValue)
                .orElse(null);

        entry.setPeriod(request.period());
        entry.setActualValue(request.actualValue());
        entry.setGapToTarget(round(goal.getTargetValue() - request.actualValue()));
        entry.setTrend(computeTrend(goal, previousActual, request.actualValue()));
        entry.setCommentary(request.commentary());

        entry = progressRepository.save(entry);
        auditLogWriter.record("UPDATE_PROGRESS", "GoalProgressEntry", entry.getId());
        return progressMapper.toResponse(entry);
    }

    @Override
    @Transactional
    public ProgressResponse confirm(Long goalId, Long progressId) {
        goalService.findVisibleOrThrow(goalId);
        GoalProgressEntry entry = loadInGoal(goalId, progressId);
        if (entry.getStatus() == ProgressStatus.CONFIRMED) {
            throw new ConflictException("Progress entry is already confirmed");
        }
        entry.setStatus(ProgressStatus.CONFIRMED);
        entry = progressRepository.save(entry);
        auditLogWriter.record("CONFIRM_PROGRESS", "GoalProgressEntry", entry.getId());
        return progressMapper.toResponse(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProgressResponse> list(Long goalId, Pageable pageable) {
        goalService.findVisibleOrThrow(goalId);
        return progressRepository.findByGoal_Id(goalId, pageable).map(progressMapper::toResponse);
    }

    private ProgressTrend computeTrend(DEIGoal goal, Double previousActual, double currentActual) {
        if (previousActual == null) {
            return ProgressTrend.STATIC;
        }
        double delta = currentActual - previousActual;
        if (delta == 0.0) {
            return ProgressTrend.STATIC;
        }
        boolean higherIsBetter = goal.getTargetValue() >= goal.getBaselineValue();
        boolean movingUp = delta > 0;
        return (movingUp == higherIsBetter) ? ProgressTrend.IMPROVING : ProgressTrend.WORSENING;
    }

    private GoalProgressEntry loadInGoal(Long goalId, Long progressId) {
        GoalProgressEntry entry = progressRepository.findById(progressId)
                .orElseThrow(() -> new ResourceNotFoundException("GoalProgressEntry", progressId));
        if (!entry.getGoal().getId().equals(goalId)) {
            throw new ForbiddenOperationException("Progress entry does not belong to goal " + goalId);
        }
        return entry;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
