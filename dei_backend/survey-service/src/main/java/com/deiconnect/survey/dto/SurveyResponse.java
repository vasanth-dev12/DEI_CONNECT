package com.deiconnect.survey.dto;

import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.enums.SurveyType;

import java.time.LocalDate;
import java.util.List;

public record SurveyResponse(
        Long surveyId,
        String surveyName,
        SurveyType surveyType,
        LocalDate launchDate,
        LocalDate closeDate,
        Boolean anonymised,
        Integer minResponseThreshold,
        SurveyStatus status,
        Long creatorManagerId,
        Boolean respondedByMe,
        List<QuestionResponse> questions
) {
}
