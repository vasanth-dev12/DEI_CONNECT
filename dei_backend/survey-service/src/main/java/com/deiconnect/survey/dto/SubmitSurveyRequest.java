package com.deiconnect.survey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitSurveyRequest(

        @NotEmpty @Valid List<AnswerItem> answers
) {
}
