package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.DemographicDimension;

import java.util.List;

public record GenerateSnapshotResult(
        DemographicDimension dimension,
        long totalConsentedConsidered,
        List<RepresentationSnapshotResponse> snapshots,
        int suppressedGroupCount
) {
}
