package com.deiconnect.erg.dto;

import java.time.LocalDate;

public record EventParticipationResponse(
        Long participationId,
        Long eventId,
        String eventName,
        Long employeeId,
        String employeeName,
        LocalDate registrationDate
) {
}
