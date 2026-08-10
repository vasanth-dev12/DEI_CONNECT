package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.SubmitAcknowledgement;
import com.deiconnect.survey.dto.SubmitSurveyRequest;
import com.deiconnect.survey.dto.SummaryResponse;
import com.deiconnect.survey.service.SurveyResponseService;
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
@RequestMapping("/api/surveys/{surveyId}")
@RequiredArgsConstructor
public class SurveyResponseController {

    private final SurveyResponseService responseService;

    @PostMapping("/responses")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<SubmitAcknowledgement> submit(@PathVariable Long surveyId,
                                                        @Valid @RequestBody SubmitSurveyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(responseService.submit(surveyId, request));
    }

    @GetMapping("/summaries")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<SummaryResponse>> summaries(@PathVariable Long surveyId,
                                                           @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(responseService.getSummaries(surveyId, pageable));
    }

    @PutMapping("/summaries/{summaryId}/publish")
    @PreAuthorize("hasAnyRole('DEI_MANAGER','ADMIN')")
    public ResponseEntity<SummaryResponse> publishSummary(@PathVariable Long surveyId,
                                                          @PathVariable Long summaryId) {
        return ResponseEntity.ok(responseService.publishSummary(surveyId, summaryId));
    }
}
