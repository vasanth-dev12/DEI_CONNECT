package com.deiconnect.iam.mapper;

import com.deiconnect.iam.dto.UserResponse;
import com.deiconnect.iam.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return toResponse(user, false);
    }

    public UserResponse toResponse(User user, boolean includeSalary) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmployeeId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartmentId(),
                user.getDepartmentName(),
                user.getGradeId(),
                user.getStatus(),
                user.getManager() != null ? user.getManager().getId() : null,
                user.getManager() != null ? user.getManager().getName() : null,
                user.getHr() != null ? user.getHr().getId() : null,
                user.getHr() != null ? user.getHr().getName() : null,
                user.getCreatedDate(),
                user.getLastModifiedDate(),
                includeSalary ? user.getSalary() : null,
                user.getYearsOfExperience());
    }
}
