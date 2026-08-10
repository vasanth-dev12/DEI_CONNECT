package com.deiconnect.payequity.dto;

import com.deiconnect.payequity.enums.FlagStatus;

import java.time.Instant;

public record PayGapFlagResponse(
        Long id,
        Long analysisId,
        Long departmentId,
        Long gradeId,
        String groupName,
        Double gapPercent,
        Integer affectedEmployeeCount,
        Long remediationOwnerId,
        String remediationOwnerName,
        FlagStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
