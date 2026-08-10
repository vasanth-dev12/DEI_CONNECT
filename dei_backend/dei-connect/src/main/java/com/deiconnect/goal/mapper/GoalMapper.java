package com.deiconnect.goal.mapper;

import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.entity.DEIGoal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public GoalResponse toResponse(DEIGoal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getDimension(),
                goal.getTargetGroup(),
                goal.getBaselineValue(),
                goal.getTargetValue(),
                goal.getTargetYear(),
                goal.getOwner() == null ? null : goal.getOwner().getId(),
                goal.getOwner() == null ? null : goal.getOwner().getName(),
                goal.getStatus(),
                goal.getCreatedDate(),
                goal.getLastModifiedDate());
    }
}
