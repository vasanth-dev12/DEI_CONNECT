package com.deiconnect.iam.controller;

import com.deiconnect.common.enums.Role;
import com.deiconnect.iam.dto.AdminCreateUserRequest;
import com.deiconnect.iam.dto.AdminUpdateUserRequest;
import com.deiconnect.iam.dto.ScopeValueOption;
import com.deiconnect.iam.dto.UpdateProfileRequest;
import com.deiconnect.iam.dto.UserResponse;
import com.deiconnect.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.adminCreate(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DEI_MANAGER','HR_BIZ_PARTNER','ERG_LEAD','EXECUTIVE','ADMIN')")
    public ResponseEntity<Page<UserResponse>> list(@RequestParam(required = false) Role role,
                                                   @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.list(role, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> currentProfile() {
        return ResponseEntity.ok(userService.getCurrentProfile());
    }

    @GetMapping("/scope-values")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScopeValueOption>> scopeValues(@RequestParam String scope) {
        return ResponseEntity.ok(userService.getScopeValues(scope));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/internal/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getByIdInternal(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getByIdInternal(id));
    }

    @PostMapping("/internal/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getByIdsInternal(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(userService.getByIdsInternal(ids));
    }

    @GetMapping("/internal/by-manager/{managerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getEmployeesByManagerInternal(@PathVariable Long managerId) {
        return ResponseEntity.ok(userService.getEmployeesByManagerInternal(managerId));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateCurrentProfile(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminUpdate(@PathVariable Long id,
                                                    @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(userService.adminUpdate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
