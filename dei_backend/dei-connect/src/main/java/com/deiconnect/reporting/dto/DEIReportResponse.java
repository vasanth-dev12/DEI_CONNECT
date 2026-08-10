package com.deiconnect.reporting.dto;

import com.deiconnect.reporting.enums.ReportMetric;
import com.deiconnect.reporting.enums.ReportScope;
import com.deiconnect.reporting.enums.ReportStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record DEIReportResponse(
        Long id,
        ReportScope scope,
        String scopeValue,
        Set<ReportMetric> metrics,
        LocalDate generatedDate,
        ReportStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
