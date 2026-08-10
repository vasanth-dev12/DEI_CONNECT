package com.deiconnect.payequity.dto;

import com.deiconnect.payequity.enums.FlagStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePayGapFlagRequest(
        @NotNull Long remediationOwnerId,
        @NotNull FlagStatus status
) {
}
