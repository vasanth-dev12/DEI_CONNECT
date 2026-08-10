package com.deiconnect.survey.service;

import com.deiconnect.survey.dto.SubmitAcknowledgement;
import com.deiconnect.survey.dto.SubmitSurveyRequest;
import com.deiconnect.survey.dto.SummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SurveyResponseService {

    SubmitAcknowledgement submit(Long surveyId, SubmitSurveyRequest request);

    Page<SummaryResponse> getSummaries(Long surveyId, Pageable pageable);

    SummaryResponse publishSummary(Long surveyId, Long summaryId);
}
