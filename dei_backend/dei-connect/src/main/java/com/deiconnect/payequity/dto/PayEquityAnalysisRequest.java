package com.deiconnect.payequity.dto;

import com.deiconnect.payequity.enums.ControlVariable;
import com.deiconnect.payequity.enums.PayDimension;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record PayEquityAnalysisRequest(
        @NotBlank @Size(max = 80) String analysisPeriod,
        @NotNull PayDimension dimension,
        @NotEmpty Set<ControlVariable> controlVariables
) {
}
