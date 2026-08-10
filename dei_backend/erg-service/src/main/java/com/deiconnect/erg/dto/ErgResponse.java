package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.ErgFocus;
import com.deiconnect.erg.enums.ErgStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ErgResponse(
        Long ergId,
        String ergName,
        ErgFocus focus,
        String mission,
        Long executiveSponsorId,
        Long ergLeadId,
        Integer memberCount,
        LocalDate foundedDate,
        ErgStatus status,
        Instant createdDate,
        Instant lastModifiedDate,
        Long creatorManagerId,
        String creatorManagerName
) {
}
