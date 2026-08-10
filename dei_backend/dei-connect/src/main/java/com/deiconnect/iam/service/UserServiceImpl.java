package com.deiconnect.iam.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.iam.dto.AdminCreateUserRequest;
import com.deiconnect.iam.dto.AdminUpdateUserRequest;
import com.deiconnect.iam.dto.ScopeValueOption;
import com.deiconnect.iam.dto.UpdateProfileRequest;
import com.deiconnect.iam.dto.UserResponse;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.enums.DepartmentName;
import com.deiconnect.iam.enums.GradeName;
import com.deiconnect.iam.enums.UserStatus;
import com.deiconnect.iam.mapper.UserMapper;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public UserResponse adminCreate(AdminCreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        if (userRepository.existsByEmployeeId(request.employeeId())) {
            throw new ConflictException("EmployeeID already registered: " + request.employeeId());
        }

        User manager = null;
        User hr = null;

        if (request.role() == Role.EMPLOYEE) {
            if (request.managerId() == null || request.hrId() == null) {
                throw new ConflictException("Manager and HR assignments are required for Employee users");
            }
            manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.managerId()));
            if (manager.getRole() != Role.DEI_MANAGER) {
                throw new ConflictException("Assigned manager must have role DEI_MANAGER");
            }
            hr = userRepository.findById(request.hrId())
                    .orElseThrow(() -> new ResourceNotFoundException("HR Partner not found with ID: " + request.hrId()));
            if (hr.getRole() != Role.HR_BIZ_PARTNER) {
                throw new ConflictException("Assigned HR must have role HR_BIZ_PARTNER");
            }
        }

        User user = User.builder()
                .employeeId(request.employeeId())
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .departmentId(request.departmentName().getId())
                .departmentName(request.departmentName())
                .gradeId(request.gradeId())
                .status(request.status() == null ? UserStatus.ACTIVE : request.status())
                .manager(manager)
                .hr(hr)
                .salary(request.salary())
                .yearsOfExperience(request.yearsOfExperience())
                .build();

        user = userRepository.save(user);
        auditLogWriter.record("ADMIN_CREATE_USER", "User", user.getId());
        return userMapper.toResponse(user, true);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile() {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        User user = loadCurrentUser();
        return userMapper.toResponse(user, canSeeSalary(principal, user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        User user = findOrThrow(id);

        if (principal.getRole() == Role.EMPLOYEE && !principal.getId().equals(id)) {
            throw new ForbiddenOperationException("You may only access your own profile");
        }
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (!principal.getId().equals(id) && (user.getManager() == null || !user.getManager().getId().equals(principal.getId()))) {
                throw new ForbiddenOperationException("You may only access employees assigned to you");
            }
        }
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            if (!principal.getId().equals(id) && (user.getHr() == null || !user.getHr().getId().equals(principal.getId()))) {
                throw new ForbiddenOperationException("You may only access employees assigned to you");
            }
        }
        return userMapper.toResponse(user, canSeeSalary(principal, user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByIdInternal(Long id) {
        User user = findOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getEmployeesByManagerInternal(Long managerId) {
        if (managerId == null) {
            return List.of();
        }
        return userRepository
                .findByManager_IdAndRoleAndStatus(managerId, Role.EMPLOYEE, UserStatus.ACTIVE).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getByIdsInternal(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllById(ids).stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Role role, Pageable pageable) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();

        boolean employeeScope = (role == null || role == Role.EMPLOYEE);

        if (principal.getRole() == Role.DEI_MANAGER && employeeScope) {
            Page<User> page = userRepository.findByManager_IdAndRole(principal.getId(), Role.EMPLOYEE, pageable);
            return page.map(u -> userMapper.toResponse(u, canSeeSalary(principal, u)));
        }
        if (principal.getRole() == Role.HR_BIZ_PARTNER && employeeScope) {
            Page<User> page = userRepository.findByHr_IdAndRole(principal.getId(), Role.EMPLOYEE, pageable);
            return page.map(u -> userMapper.toResponse(u, canSeeSalary(principal, u)));
        }

        Page<User> page = (role == null)
                ? userRepository.findAll(pageable)
                : userRepository.findByRole(role, pageable);
        return page.map(u -> userMapper.toResponse(u, canSeeSalary(principal, u)));
    }

    @Override
    @Transactional
    public UserResponse updateCurrentProfile(UpdateProfileRequest request) {
        User user = loadCurrentUser();
        applyEmailChange(user, request.email());
        user.setName(request.name());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user = userRepository.save(user);
        auditLogWriter.record("UPDATE_PROFILE", "User", user.getId());
        return userMapper.toResponse(user, canSeeSalary(SecurityUtils.requireCurrentPrincipal(), user));
    }

    @Override
    @Transactional
    public UserResponse adminUpdate(Long id, AdminUpdateUserRequest request) {
        User user = findOrThrow(id);
        applyEmailChange(user, request.email());
        user.setName(request.name());
        user.setRole(request.role());
        user.setStatus(request.status());
        user.setDepartmentId(request.departmentName().getId());
        user.setDepartmentName(request.departmentName());
        user.setGradeId(request.gradeId());

        User manager = null;
        User hr = null;

        if (request.role() == Role.EMPLOYEE) {
            if (request.managerId() == null || request.hrId() == null) {
                throw new ConflictException("Manager and HR assignments are required for Employee users");
            }
            manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.managerId()));
            if (manager.getRole() != Role.DEI_MANAGER) {
                throw new ConflictException("Assigned manager must have role DEI_MANAGER");
            }
            hr = userRepository.findById(request.hrId())
                    .orElseThrow(() -> new ResourceNotFoundException("HR Partner not found with ID: " + request.hrId()));
            if (hr.getRole() != Role.HR_BIZ_PARTNER) {
                throw new ConflictException("Assigned HR must have role HR_BIZ_PARTNER");
            }
        }

        user.setManager(manager);
        user.setHr(hr);
        user.setSalary(request.salary());
        user.setYearsOfExperience(request.yearsOfExperience());

        user = userRepository.save(user);
        auditLogWriter.record("ADMIN_UPDATE_USER", "User", user.getId());
        return userMapper.toResponse(user, true);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        User user = findOrThrow(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        auditLogWriter.record("ADMIN_DEACTIVATE_USER", "User", user.getId());
    }

    private boolean canSeeSalary(DeiUserPrincipal principal, User target) {
        if (principal.getRole() == Role.ADMIN) {
            return true;
        }
        if (principal.getRole() == Role.HR_BIZ_PARTNER) {
            return target.getHr() != null && target.getHr().getId().equals(principal.getId());
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScopeValueOption> getScopeValues(String scope) {
        if ("DEPARTMENT".equalsIgnoreCase(scope)) {
            return userRepository.findDistinctDepartments().stream()
                    .map(row -> {
                        Long deptId = (Long) row[0];
                        DepartmentName name = (DepartmentName) row[1];
                        if (name == null) {
                            name = DepartmentName.fromId(deptId);
                        }
                        String label = name != null ? name.name() : "Department " + deptId;
                        return new ScopeValueOption(String.valueOf(deptId), label);
                    })
                    .toList();
        }
        if ("GRADE".equalsIgnoreCase(scope)) {
            return userRepository.findDistinctGrades().stream()
                    .map(gradeId -> {
                        GradeName grade = GradeName.fromId(gradeId);
                        String label = grade != null ? grade.name() : "Grade " + gradeId;
                        return new ScopeValueOption(String.valueOf(gradeId), label);
                    })
                    .toList();
        }
        return List.of();
    }

    private User loadCurrentUser() {
        Long id = SecurityUtils.getCurrentUserId();
        return findOrThrow(id);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void applyEmailChange(User user, String newEmail) {
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("Email already in use: " + newEmail);
        }
        user.setEmail(newEmail);
    }
}
