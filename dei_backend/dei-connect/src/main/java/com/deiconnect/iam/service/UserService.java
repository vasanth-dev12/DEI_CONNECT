package com.deiconnect.iam.service;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.dto.AdminCreateUserRequest;
import com.deiconnect.iam.dto.AdminUpdateUserRequest;
import com.deiconnect.iam.dto.ScopeValueOption;
import com.deiconnect.iam.dto.UpdateProfileRequest;
import com.deiconnect.iam.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse adminCreate(AdminCreateUserRequest request);

    UserResponse getCurrentProfile();

    UserResponse getById(Long id);

    UserResponse getByIdInternal(Long id);

    List<UserResponse> getByIdsInternal(List<Long> ids);

    List<UserResponse> getEmployeesByManagerInternal(Long managerId);

    Page<UserResponse> list(Role role, Pageable pageable);

    UserResponse updateCurrentProfile(UpdateProfileRequest request);

    UserResponse adminUpdate(Long id, AdminUpdateUserRequest request);

    void deactivate(Long id);

    List<ScopeValueOption> getScopeValues(String scope);
}
