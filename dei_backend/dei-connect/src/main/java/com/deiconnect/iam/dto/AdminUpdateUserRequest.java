package com.deiconnect.iam.dto;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminUpdateUserRequest(

        @NotBlank @Size(max = 150)
        String name,

        @NotBlank @Email @Size(max = 190)
        String email,

        @NotNull
        Role role,

        @NotNull
        UserStatus status,

        Long departmentId,

        @NotNull
        DepartmentName departmentName,

        @PositiveOrZero(message = "gradeId must not be negative")
        Long gradeId,

        Long managerId,

        Long hrId,

        @NotNull @PositiveOrZero
        BigDecimal salary,

        @NotNull @PositiveOrZero
        Integer yearsOfExperience
) {
}
