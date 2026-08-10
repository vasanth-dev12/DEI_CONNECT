package com.deiconnect.goal.controller;

import com.deiconnect.goal.dto.CreateProgressRequest;
import com.deiconnect.goal.dto.ProgressResponse;
import com.deiconnect.goal.dto.UpdateProgressRequest;
import com.deiconnect.goal.service.GoalProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals/{goalId}/progress")
@RequiredArgsConstructor
public class GoalProgressController {

    private final GoalProgressService progressService;

    @PostMapping
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<ProgressResponse> create(@PathVariable Long goalId,
                                                    @Valid @RequestBody CreateProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(progressService.create(goalId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<ProgressResponse>> list(@PathVariable Long goalId,
                                                       @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(progressService.list(goalId, pageable));
    }

    @PutMapping("/{progressId}")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<ProgressResponse> update(@PathVariable Long goalId,
                                                   @PathVariable Long progressId,
                                                   @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(progressService.update(goalId, progressId, request));
    }

    @PutMapping("/{progressId}/confirm")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<ProgressResponse> confirm(@PathVariable Long goalId,
                                                    @PathVariable Long progressId) {
        return ResponseEntity.ok(progressService.confirm(goalId, progressId));
    }
}
