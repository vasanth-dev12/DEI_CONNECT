package com.deiconnect.goal.dto;

import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateGoalRequest(

        @NotBlank @Size(max = 200) String goalName,

        @NotNull GoalDimension dimension,

        @Size(max = 120) String targetGroup,

        @NotNull @PositiveOrZero Double baselineValue,

        @NotNull @PositiveOrZero Double targetValue,

        @NotNull @Positive Integer targetYear,

        @NotNull GoalStatus status
) {
}
