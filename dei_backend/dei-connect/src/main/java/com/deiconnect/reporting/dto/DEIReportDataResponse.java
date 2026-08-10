package com.deiconnect.reporting.dto;

import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.reporting.enums.ReportScope;

import java.time.LocalDate;
import java.util.List;

public record DEIReportDataResponse(
        Long reportId,
        ReportScope scope,
        String scopeValue,
        LocalDate generatedDate,
        List<RepresentationSnapshotResponse> representation,
        Double inclusionIndex,
        Double ergMembershipRate,
        Double goalAttainmentRate,
        Double payEquityGap
) {
}
