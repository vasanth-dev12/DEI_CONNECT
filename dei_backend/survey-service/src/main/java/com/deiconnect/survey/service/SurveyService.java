package com.deiconnect.survey.service;

import com.deiconnect.survey.dto.CreateSurveyRequest;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.dto.UpdateSurveyRequest;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.enums.SurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyService {

    SurveyResponse create(CreateSurveyRequest request);

    SurveyResponse update(Long id, UpdateSurveyRequest request);

    void delete(Long id);

    Page<SurveyResponse> list(SurveyStatus status, Pageable pageable);

    SurveyResponse getById(Long id);

    SurveyResponse launch(Long id);

    SurveyResponse close(Long id);

    SurveyResponse publishResults(Long id);

    InclusionSurvey findOrThrow(Long id);

    Double getAverageInclusionIndex(String scope, String scopeValue);
}
