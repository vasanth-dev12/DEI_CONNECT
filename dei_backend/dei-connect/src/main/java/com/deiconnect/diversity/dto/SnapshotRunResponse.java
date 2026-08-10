package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.iam.enums.DepartmentName;

import java.time.LocalDate;
import java.util.List;

public record SnapshotRunResponse(

        Long snapshotId,

        LocalDate snapshotDate,
        DemographicDimension dimension,
        Long departmentId,
        DepartmentName departmentName,

        SnapshotStatus status,

        int groupCount,

        int suppressedGroupCount,

        int totalHeadCount,

        int totalConsidered,

        List<RepresentationSnapshotResponse> groups
) {
}
