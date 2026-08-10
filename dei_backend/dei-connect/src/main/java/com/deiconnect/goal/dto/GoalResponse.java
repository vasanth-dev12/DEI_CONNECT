package com.deiconnect.goal.dto;

import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;

import java.time.Instant;

public record GoalResponse(
        Long goalId,
        String goalName,
        GoalDimension dimension,
        String targetGroup,
        Double baselineValue,
        Double targetValue,
        Integer targetYear,
        Long ownerId,
        String ownerName,
        GoalStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
