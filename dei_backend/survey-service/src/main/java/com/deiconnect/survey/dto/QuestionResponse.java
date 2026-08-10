package com.deiconnect.survey.dto;

import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SurveyDimension;

public record QuestionResponse(
        Long questionId,
        Long surveyId,
        String questionText,
        QuestionType questionType,
        SurveyDimension dimension,
        Boolean mandatory,
        Integer sequenceOrder,
        Long creatorManagerId,
        String creatorManagerName
) {
}
