package com.deiconnect.goal.mapper;

import com.deiconnect.goal.dto.ProgressResponse;
import com.deiconnect.goal.entity.GoalProgressEntry;
import org.springframework.stereotype.Component;

@Component
public class GoalProgressMapper {

    public ProgressResponse toResponse(GoalProgressEntry entry) {
        return new ProgressResponse(
                entry.getId(),
                entry.getGoal() == null ? null : entry.getGoal().getId(),
                entry.getPeriod(),
                entry.getActualValue(),
                entry.getGapToTarget(),
                entry.getTrend(),
                entry.getCommentary(),
                entry.getStatus(),
                entry.getCreatedDate(),
                entry.getLastModifiedDate());
    }
}
