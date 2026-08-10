package com.deiconnect.erg.client;

import com.deiconnect.common.enums.Role;

import java.time.Instant;

public record UserResponse(
        Long userId,
        String employeeId,
        String name,
        String email,
        Role role,
        Long departmentId,
        Long gradeId,
        String status,
        Long managerId,
        String managerName,
        Long hrId,
        String hrName,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
