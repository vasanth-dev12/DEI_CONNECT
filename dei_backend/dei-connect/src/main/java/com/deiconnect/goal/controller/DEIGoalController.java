package com.deiconnect.goal.controller;

import com.deiconnect.goal.dto.CreateGoalRequest;
import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.dto.UpdateGoalRequest;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import com.deiconnect.goal.service.DEIGoalService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class DEIGoalController {

    private final DEIGoalService goalService;

    @PostMapping
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<GoalResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(goalService.update(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<GoalResponse>> search(@RequestParam(required = false) GoalDimension dimension,
                                                     @RequestParam(required = false) GoalStatus status,
                                                     @RequestParam(required = false) Long ownerId,
                                                     @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(goalService.search(dimension, status, ownerId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<GoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getById(id));
    }
}
