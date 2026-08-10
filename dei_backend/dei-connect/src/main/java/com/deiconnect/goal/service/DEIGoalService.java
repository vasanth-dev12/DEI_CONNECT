package com.deiconnect.goal.service;

import com.deiconnect.goal.dto.CreateGoalRequest;
import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.dto.UpdateGoalRequest;
import com.deiconnect.goal.entity.DEIGoal;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DEIGoalService {

    GoalResponse create(CreateGoalRequest request);

    GoalResponse update(Long id, UpdateGoalRequest request);

    GoalResponse getById(Long id);

    Page<GoalResponse> search(GoalDimension dimension, GoalStatus status, Long ownerId,
                              Pageable pageable);

    DEIGoal findOrThrow(Long id);

    DEIGoal findVisibleOrThrow(Long id);
}
