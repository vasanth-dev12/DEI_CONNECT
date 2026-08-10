package com.deiconnect.survey.dto;

public record SubmitAcknowledgement(
        Long surveyId,
        boolean accepted,
        String message
) {
}
