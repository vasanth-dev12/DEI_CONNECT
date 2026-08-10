package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.DemographicDimension;
import com.deiconnect.iam.enums.DepartmentName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record GenerateSnapshotRequest(

        @NotNull @PastOrPresent LocalDate snapshotDate,

        DepartmentName departmentName,

        @NotNull DemographicDimension dimension
) {
    public Long departmentId() {
        return departmentName == null ? null : departmentName.getId();
    }
}
