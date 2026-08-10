package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.MembershipRole;
import com.deiconnect.erg.enums.MembershipStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMembershipRequest(

        @NotNull MembershipRole role,

        @NotNull MembershipStatus status
) {
}
