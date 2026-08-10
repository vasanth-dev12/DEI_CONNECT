package com.deiconnect.reporting.dto;

import com.deiconnect.reporting.enums.ReportMetric;
import com.deiconnect.reporting.enums.ReportScope;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DEIReportRequest(
        @NotNull ReportScope scope,
        @Size(max = 120) String scopeValue,
        @NotEmpty Set<ReportMetric> metrics
) {
}
