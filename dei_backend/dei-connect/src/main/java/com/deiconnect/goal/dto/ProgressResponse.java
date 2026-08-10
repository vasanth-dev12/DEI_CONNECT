package com.deiconnect.goal.dto;

import com.deiconnect.goal.enums.ProgressStatus;
import com.deiconnect.goal.enums.ProgressTrend;

import java.time.Instant;

public record ProgressResponse(
        Long progressId,
        Long goalId,
        String period,
        Double actualValue,
        Double gapToTarget,
        ProgressTrend trend,
        String commentary,
        ProgressStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
