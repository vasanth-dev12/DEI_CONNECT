package com.deiconnect.iam.dto;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record UserResponse(
        Long userId,
        String employeeId,
        String name,
        String email,
        Role role,
        Long departmentId,
        DepartmentName departmentName,
        Long gradeId,
        UserStatus status,
        Long managerId,
        String managerName,
        Long hrId,
        String hrName,
        Instant createdDate,
        Instant lastModifiedDate,
        BigDecimal salary,
        Integer yearsOfExperience
) {
}
