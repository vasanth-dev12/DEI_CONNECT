package com.deiconnect.diversity.controller;

import com.deiconnect.diversity.dto.DemographicProfileRequest;
import com.deiconnect.diversity.dto.DemographicProfileResponse;
import com.deiconnect.diversity.service.DemographicProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demographic-profiles")
@RequiredArgsConstructor
public class DemographicProfileController {

    private final DemographicProfileService demographicProfileService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<DemographicProfileResponse> create(
            @Valid @RequestBody DemographicProfileRequest payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demographicProfileService.createOwn(payload));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<DemographicProfileResponse> getOwn() {
        return ResponseEntity.ok(demographicProfileService.getOwn());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<DemographicProfileResponse> updateOwn(
            @Valid @RequestBody DemographicProfileRequest payload) {
        return ResponseEntity.ok(demographicProfileService.updateOwn(payload));
    }

}