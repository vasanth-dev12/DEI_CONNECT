package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.EventStatus;
import com.deiconnect.erg.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEventRequest(

        @NotBlank @Size(max = 200) String eventName,

        @NotNull EventType eventType,

        @NotNull LocalDate date,

        @PositiveOrZero Integer attendeeCount,

        @PositiveOrZero BigDecimal budgetSpent,

        @NotNull EventStatus status
) {
}
