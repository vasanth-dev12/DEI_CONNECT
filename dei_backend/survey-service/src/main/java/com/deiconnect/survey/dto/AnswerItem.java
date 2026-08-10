package com.deiconnect.survey.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerItem(

        @NotNull Long questionId,

        @NotNull(message = "An answer value is required")
        Integer numericValue
) {
}
