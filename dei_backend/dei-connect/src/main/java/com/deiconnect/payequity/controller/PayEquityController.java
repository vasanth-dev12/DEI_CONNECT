package com.deiconnect.payequity.controller;

import com.deiconnect.payequity.dto.PayEquityAnalysisRequest;
import com.deiconnect.payequity.dto.PayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PayGapFlagResponse;
import com.deiconnect.payequity.dto.PublishedPayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PublishedPayGapFlagResponse;
import com.deiconnect.payequity.dto.UpdatePayGapFlagRequest;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.PayDimension;
import com.deiconnect.payequity.service.PayEquityAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pay-equity")
@RequiredArgsConstructor
public class PayEquityController {

    private final PayEquityAnalysisService service;

    @PostMapping("/analyses")
    @PreAuthorize("hasRole('HR_BIZ_PARTNER')")
    public ResponseEntity<PayEquityAnalysisResponse> create(
            @Valid @RequestBody PayEquityAnalysisRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAnalysis(request));
    }

    @PutMapping("/analyses/{id}")
    @PreAuthorize("hasRole('HR_BIZ_PARTNER')")
    public ResponseEntity<PayEquityAnalysisResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PayEquityAnalysisRequest request) {
        return ResponseEntity.ok(service.updateAnalysis(id, request));
    }

    @PutMapping("/analyses/{id}/publish")
    @PreAuthorize("hasRole('HR_BIZ_PARTNER')")
    public ResponseEntity<PayEquityAnalysisResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(service.publishAnalysis(id));
    }

    @PostMapping("/analyses/{id}/compute")
    @PreAuthorize("hasAnyRole('HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<PayEquityAnalysisResponse> compute(@PathVariable Long id) {
        return ResponseEntity.ok(service.computeFromWorkforce(id));
    }

    @GetMapping("/analyses")
    @PreAuthorize("hasAnyRole('HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<Page<PayEquityAnalysisResponse>> list(
            @RequestParam(required = false) PayDimension dimension,
            @RequestParam(required = false) AnalysisStatus status,
            @RequestParam(required = false) Long hrId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listAnalyses(dimension, status, hrId, pageable));
    }

    @GetMapping("/analyses/{id}")
    @PreAuthorize("hasAnyRole('HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<PayEquityAnalysisResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAnalysisById(id));
    }

    @PutMapping("/analyses/{analysisId}/flags/{flagId}")
    @PreAuthorize("hasRole('HR_BIZ_PARTNER')")
    public ResponseEntity<PayGapFlagResponse> updateFlag(
            @PathVariable Long analysisId,
            @PathVariable Long flagId,
            @Valid @RequestBody UpdatePayGapFlagRequest request) {
        return ResponseEntity.ok(service.updateFlag(analysisId, flagId, request));
    }

    @GetMapping("/analyses/{analysisId}/flags")
    @PreAuthorize("hasAnyRole('HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<List<PayGapFlagResponse>> listFlags(@PathVariable Long analysisId) {
        return ResponseEntity.ok(service.listFlags(analysisId));
    }

    @GetMapping("/published/analyses")
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<Page<PublishedPayEquityAnalysisResponse>> listPublished(
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listPublishedAnalyses(pageable));
    }

    @GetMapping("/published/analyses/{id}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<PublishedPayEquityAnalysisResponse> getPublishedById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPublishedAnalysisById(id));
    }

    @GetMapping("/published/analyses/{analysisId}/flags")
    @PreAuthorize("hasAnyRole('DEI_MANAGER', 'EXECUTIVE', 'HR_BIZ_PARTNER', 'ADMIN')")
    public ResponseEntity<List<PublishedPayGapFlagResponse>> listPublishedFlags(
            @PathVariable Long analysisId) {
        return ResponseEntity.ok(service.listPublishedFlags(analysisId));
    }
}
