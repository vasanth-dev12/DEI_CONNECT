package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.diversity.enums.SnapshotStatus;
import com.deiconnect.iam.enums.DepartmentName;

import java.time.LocalDate;

public record RepresentationSnapshotResponse(
        Long snapshotId,
        LocalDate snapshotDate,
        Long departmentId,
        DepartmentName departmentName,
        DemographicDimension dimension,
        String groupName,
        Integer count,
        Double percentage,
        SnapshotStatus status,
        boolean suppressed
) {
}
