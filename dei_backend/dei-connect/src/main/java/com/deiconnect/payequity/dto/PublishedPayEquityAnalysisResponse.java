package com.deiconnect.payequity.dto;

import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.ControlVariable;
import com.deiconnect.payequity.enums.PayDimension;

import java.time.Instant;
import java.util.Set;

public record PublishedPayEquityAnalysisResponse(
        Long id,
        String analysisPeriod,
        PayDimension dimension,
        Set<ControlVariable> controlVariables,
        Double medianGapPercent,
        Double adjustedGapPercent,
        Double significanceLevel,
        AnalysisStatus status,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
