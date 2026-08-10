package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.iam.enums.DepartmentName;

import java.time.LocalDate;
import java.util.List;

public record SnapshotGroupResponse(
        LocalDate snapshotDate,
        DemographicDimension dimension,
        Long departmentId,
        DepartmentName departmentName,
        int totalHeadCount,
        int groupCount,
        int suppressedGroupCount,
        int totalConsidered,
        List<RepresentationSnapshotResponse> groups
) {
}
