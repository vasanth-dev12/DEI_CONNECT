package com.deiconnect.survey.controller;

import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.dto.UpdateQuestionRequest;
import com.deiconnect.survey.service.SurveyQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/surveys/{surveyId}/questions")
@RequiredArgsConstructor
public class SurveyQuestionController {

    private final SurveyQuestionService questionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','DEI_MANAGER','EXECUTIVE','ADMIN')")
    public ResponseEntity<List<QuestionResponse>> list(@PathVariable Long surveyId,
                                                        @RequestParam(required = false) Long managerId) {
        return ResponseEntity.ok(questionService.listForSurvey(surveyId, managerId));
    }

    @PostMapping
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<QuestionResponse> add(@PathVariable Long surveyId,
                                                @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.add(surveyId, request));
    }

    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<QuestionResponse> update(@PathVariable Long surveyId,
                                                   @PathVariable Long questionId,
                                                   @Valid @RequestBody UpdateQuestionRequest request) {
        return ResponseEntity.ok(questionService.update(surveyId, questionId, request));
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('DEI_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long surveyId, @PathVariable Long questionId) {
        questionService.delete(surveyId, questionId);
        return ResponseEntity.noContent().build();
    }
}
