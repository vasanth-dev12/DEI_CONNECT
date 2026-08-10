package com.deiconnect.survey.dto;

import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SurveyDimension;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(

        @NotBlank @Size(max = 1000) String questionText,

        @NotNull QuestionType questionType,

        @NotNull SurveyDimension dimension,

        @NotNull Boolean mandatory,

        @PositiveOrZero Integer sequenceOrder
) {
}
