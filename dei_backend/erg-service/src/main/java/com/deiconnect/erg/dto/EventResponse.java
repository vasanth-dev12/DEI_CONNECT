package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.EventStatus;
import com.deiconnect.erg.enums.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record EventResponse(
        Long eventId,
        Long ergId,
        String eventName,
        EventType eventType,
        LocalDate date,
        Integer attendeeCount,
        BigDecimal budgetSpent,
        EventStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
