package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.MembershipRole;
import com.deiconnect.erg.enums.MembershipStatus;

import java.time.Instant;
import java.time.LocalDate;

public record MembershipResponse(
        Long membershipId,
        Long ergId,
        Long employeeUserId,
        String employeeId,
        MembershipRole role,
        LocalDate joinDate,
        MembershipStatus status,
        Instant createdDate
) {
}
