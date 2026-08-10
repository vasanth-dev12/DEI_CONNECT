package com.deiconnect.payequity.dto;

import com.deiconnect.payequity.enums.FlagStatus;

public record PublishedPayGapFlagResponse(
        Long id,
        Long analysisId,
        Long departmentId,
        Long gradeId,
        String groupName,
        Double gapPercent,
        Integer affectedEmployeeCount,
        FlagStatus status,
        boolean suppressed
) {
}
