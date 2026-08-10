package com.deiconnect.survey.dto;

import com.deiconnect.survey.enums.SurveyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateSurveyRequest(

        @NotBlank @Size(max = 200) String surveyName,

        @NotNull SurveyType surveyType,

        @FutureOrPresent LocalDate launchDate,

        @FutureOrPresent LocalDate closeDate,

        Boolean anonymised,

        @NotNull @Positive Integer minResponseThreshold,

        @Valid List<CreateQuestionRequest> questions
) {

    @AssertTrue(message = "closeDate must be on or after launchDate")
    public boolean isDateRangeValid() {
        return launchDate == null || closeDate == null || !closeDate.isBefore(launchDate);
    }
}
