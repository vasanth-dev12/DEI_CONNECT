package com.deiconnect.reporting.controller;

import com.deiconnect.reporting.dto.DEIReportDataResponse;
import com.deiconnect.reporting.dto.DEIReportRequest;
import com.deiconnect.reporting.dto.DEIReportResponse;
import com.deiconnect.reporting.enums.ReportStatus;
import com.deiconnect.reporting.service.DEIReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class DEIReportController {

    private final DEIReportService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DEIReportResponse> create(@Valid @RequestBody DEIReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createReport(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DEIReportResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DEIReportRequest request) {
        return ResponseEntity.ok(service.updateReport(id, request));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DEIReportResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(service.publishReport(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'HR_BIZ_PARTNER', 'EXECUTIVE', 'ADMIN')")
    public ResponseEntity<Page<DEIReportResponse>> list(
            @RequestParam(required = false) ReportStatus status,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listReports(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'HR_BIZ_PARTNER', 'EXECUTIVE', 'ADMIN')")
    public ResponseEntity<DEIReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReportById(id));
    }

    @GetMapping("/{id}/data")
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'HR_BIZ_PARTNER', 'EXECUTIVE', 'ADMIN')")
    public ResponseEntity<DEIReportDataResponse> getDashboardData(@PathVariable Long id) {
        return ResponseEntity.ok(service.computeReportData(id));
    }
}
