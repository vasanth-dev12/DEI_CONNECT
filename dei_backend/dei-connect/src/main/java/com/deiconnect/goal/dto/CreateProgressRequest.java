package com.deiconnect.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProgressRequest(

        @NotBlank @Size(max = 40) String period,

        @NotNull @PositiveOrZero Double actualValue,

        @Size(max = 1000) String commentary
) {
}
