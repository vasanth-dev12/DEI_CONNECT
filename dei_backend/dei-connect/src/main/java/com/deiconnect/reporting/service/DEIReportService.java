package com.deiconnect.reporting.service;

import com.deiconnect.reporting.dto.DEIReportDataResponse;
import com.deiconnect.reporting.dto.DEIReportRequest;
import com.deiconnect.reporting.dto.DEIReportResponse;
import com.deiconnect.reporting.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DEIReportService {

    DEIReportResponse createReport(DEIReportRequest request);

    DEIReportResponse updateReport(Long id, DEIReportRequest request);

    DEIReportResponse publishReport(Long id);

    void deleteReport(Long id);

    DEIReportResponse getReportById(Long id);

    Page<DEIReportResponse> listReports(ReportStatus status, Pageable pageable);

    DEIReportDataResponse computeReportData(Long id);
}
