package com.deiconnect.erg.dto;

import java.time.LocalDate;

public record ParticipantResponse(
        Long employeeId,
        String employeeName,
        String employeeEmail,
        LocalDate registrationDate
) {
}
