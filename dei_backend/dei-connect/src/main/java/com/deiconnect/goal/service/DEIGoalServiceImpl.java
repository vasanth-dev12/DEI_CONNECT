package com.deiconnect.goal.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.goal.dto.CreateGoalRequest;
import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.dto.UpdateGoalRequest;
import com.deiconnect.goal.entity.DEIGoal;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import com.deiconnect.goal.mapper.GoalMapper;
import com.deiconnect.goal.repository.DEIGoalRepository;
import com.deiconnect.iam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DEIGoalServiceImpl implements DEIGoalService {

    private final DEIGoalRepository goalRepository;
    private final UserRepository userRepository;
    private final GoalMapper goalMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public GoalResponse create(CreateGoalRequest request) {
        Long ownerId = SecurityUtils.requireCurrentPrincipal().getId();
        requireUserExists(ownerId);
        DEIGoal goal = DEIGoal.builder()
                .goalName(request.goalName())
                .dimension(request.dimension())
                .targetGroup(request.targetGroup())
                .baselineValue(request.baselineValue())
                .targetValue(request.targetValue())
                .targetYear(request.targetYear())
                .owner(userRepository.getReferenceById(ownerId))
                .status(GoalStatus.ACTIVE)
                .creatorManagerId(ownerId)
                .build();
        goal = goalRepository.save(goal);
        auditLogWriter.record("CREATE_GOAL", "DEIGoal", goal.getId());
        return goalMapper.toResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse update(Long id, UpdateGoalRequest request) {
        DEIGoal goal = findVisibleOrThrow(id);
        goal.setGoalName(request.goalName());
        goal.setDimension(request.dimension());
        goal.setTargetGroup(request.targetGroup());
        goal.setBaselineValue(request.baselineValue());
        goal.setTargetValue(request.targetValue());
        goal.setTargetYear(request.targetYear());
        goal.setStatus(request.status());
        goal = goalRepository.save(goal);
        auditLogWriter.record("UPDATE_GOAL", "DEIGoal", goal.getId());
        return goalMapper.toResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponse getById(Long id) {
        return goalMapper.toResponse(findVisibleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GoalResponse> search(GoalDimension dimension, GoalStatus status, Long ownerId,
                                     Pageable pageable) {
        Long creatorManagerId = (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER)
                ? SecurityUtils.getCurrentUserId()
                : null;
        return goalRepository.search(dimension, status, ownerId, creatorManagerId, pageable)
                .map(goalMapper::toResponse);
    }

    @Override
    public DEIGoal findOrThrow(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DEIGoal", id));
    }

    @Override
    public DEIGoal findVisibleOrThrow(Long id) {
        DEIGoal goal = findOrThrow(id);
        if (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER) {
            Long me = SecurityUtils.getCurrentUserId();
            if (goal.getCreatorManagerId() == null || !goal.getCreatorManagerId().equals(me)) {
                throw new ForbiddenOperationException("You may only access goals you created");
            }
        }
        return goal;
    }

    private void requireUserExists(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("User", ownerId);
        }
    }
}
