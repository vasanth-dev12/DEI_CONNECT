package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.CreateSurveyRequest;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.dto.UpdateSurveyRequest;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.service.SurveyService;
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
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyResponse> create(@Valid @RequestBody CreateSurveyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(surveyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateSurveyRequest request) {
        return ResponseEntity.ok(surveyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        surveyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<SurveyResponse>> list(@RequestParam(required = false) SurveyStatus status,
                                                     @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(surveyService.list(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<SurveyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getById(id));
    }

    @PutMapping("/{id}/launch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyResponse> launch(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.launch(id));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.close(id));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurveyResponse> publishResults(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.publishResults(id));
    }

    @GetMapping("/internal/inclusion-index/average")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Double> getAverageInclusionIndex(@RequestParam(required = false) String scope,
                                                           @RequestParam(required = false) String scopeValue) {
        return ResponseEntity.ok(surveyService.getAverageInclusionIndex(scope, scopeValue));
    }
}
