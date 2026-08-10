package com.deiconnect.survey.dto;

import com.deiconnect.survey.enums.SummaryScope;
import com.deiconnect.survey.enums.SummaryStatus;

public record SummaryResponse(
        Long summaryId,
        Long surveyId,
        SummaryScope scope,
        String scopeValue,
        Integer respondentCount,
        Double inclusionIndex,
        String keyThemeSentiment,
        SummaryStatus status,
        boolean suppressed
) {
}
