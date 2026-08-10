package com.deiconnect.diversity.controller;

import com.deiconnect.diversity.dto.GenerateSnapshotRequest;
import com.deiconnect.diversity.dto.GenerateSnapshotResult;
import com.deiconnect.diversity.dto.RepresentationSnapshotResponse;
import com.deiconnect.diversity.dto.SnapshotGroupResponse;
import com.deiconnect.diversity.dto.SnapshotRunResponse;
import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.diversity.service.RepresentationSnapshotService;
import com.deiconnect.iam.enums.DepartmentName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
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
@RequestMapping("/api/representation-snapshots")
@RequiredArgsConstructor
public class RepresentationSnapshotController {

    private final RepresentationSnapshotService representationSnapshotService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<GenerateSnapshotResult> generate(
            @Valid @RequestBody GenerateSnapshotRequest payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(representationSnapshotService.generate(payload));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<SnapshotRunResponse>> searchRuns(
            @RequestParam(required = false) DemographicDimension dimensionFilter,
            @RequestParam(required = false) DepartmentName departmentFilter,
            @RequestParam(required = false) SnapshotStatus statusFilter,
            @ParameterObject Pageable pageable) {
        Long departmentId = departmentFilter == null ? null : departmentFilter.getId();
        return ResponseEntity.ok(representationSnapshotService.searchRuns(dimensionFilter, departmentId, statusFilter, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<RepresentationSnapshotResponse>> search(
            @RequestParam(required = false) DemographicDimension dimensionFilter,
            @RequestParam(required = false) DepartmentName departmentFilter,
            @RequestParam(required = false) SnapshotStatus statusFilter,
            @ParameterObject Pageable pageable) {
        Long departmentId = departmentFilter == null ? null : departmentFilter.getId();
        return ResponseEntity.ok(representationSnapshotService.search(dimensionFilter, departmentId, statusFilter, pageable));
    }

    @GetMapping("/{snapshotId}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<RepresentationSnapshotResponse> getById(@PathVariable Long snapshotId) {
        return ResponseEntity.ok(representationSnapshotService.getById(snapshotId));
    }

    @GetMapping("/{snapshotId}/distribution")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<SnapshotGroupResponse> getDistribution(@PathVariable Long snapshotId) {
        return ResponseEntity.ok(representationSnapshotService.getDistribution(snapshotId));
    }

    @PutMapping("/runs/{snapshotRunId}/publish")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<SnapshotRunResponse> publishRun(@PathVariable Long snapshotRunId) {
        return ResponseEntity.ok(representationSnapshotService.publishRun(snapshotRunId));
    }

    @PutMapping("/{snapshotId}/publish")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<RepresentationSnapshotResponse> publish(@PathVariable Long snapshotId) {
        return ResponseEntity.ok(representationSnapshotService.publish(snapshotId));
    }

    @DeleteMapping("/runs/{snapshotRunId}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ADMIN')")
    public ResponseEntity<Void> deleteRun(@PathVariable Long snapshotRunId) {
        representationSnapshotService.deleteRun(snapshotRunId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{snapshotId}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long snapshotId) {
        representationSnapshotService.delete(snapshotId);
        return ResponseEntity.noContent().build();
    }
}