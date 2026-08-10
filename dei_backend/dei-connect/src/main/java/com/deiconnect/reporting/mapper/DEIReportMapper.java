package com.deiconnect.reporting.mapper;

import com.deiconnect.reporting.dto.DEIReportResponse;
import com.deiconnect.reporting.entity.DEIReport;
import org.springframework.stereotype.Component;

@Component
public class DEIReportMapper {

    public DEIReportResponse toResponse(DEIReport report) {
        if (report == null) {
            return null;
        }
        return new DEIReportResponse(
                report.getId(),
                report.getScope(),
                report.getScopeValue(),
                report.getMetrics(),
                report.getGeneratedDate(),
                report.getStatus(),
                report.getCreatedDate(),
                report.getLastModifiedDate()
        );
    }
}
