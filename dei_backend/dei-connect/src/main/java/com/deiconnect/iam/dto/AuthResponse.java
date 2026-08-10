package com.deiconnect.iam.dto;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.entity.User;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String employeeId,
        String name,
        String email,
        Role role
) {

    public static AuthResponse of(String token, User user) {
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmployeeId(),
                user.getName(),
                user.getEmail(),
                user.getRole());
    }
}
