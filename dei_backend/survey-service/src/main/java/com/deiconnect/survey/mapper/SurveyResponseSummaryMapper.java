package com.deiconnect.survey.mapper;

import com.deiconnect.survey.dto.SummaryResponse;
import com.deiconnect.survey.entity.SurveyResponseSummary;
import org.springframework.stereotype.Component;

@Component
public class SurveyResponseSummaryMapper {

    public SummaryResponse toResponse(SurveyResponseSummary summary, int minResponseThreshold) {
        boolean suppressed = summary.getRespondentCount() == null
                || summary.getRespondentCount() < minResponseThreshold;
        return new SummaryResponse(
                summary.getId(),
                summary.getSurvey() == null ? null : summary.getSurvey().getId(),
                summary.getScope(),
                summary.getScopeValue(),
                suppressed ? null : summary.getRespondentCount(),
                suppressed ? null : summary.getInclusionIndex(),
                suppressed ? null : summary.getKeyThemeSentiment(),
                summary.getStatus(),
                suppressed);
    }
}
