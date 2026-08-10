package com.deiconnect.survey.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "deiconnect-backend", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/users/internal/{id}")
    UserResponse getByIdInternal(@PathVariable("id") Long id);

    @PostMapping("/api/users/internal/batch")
    java.util.List<UserResponse> getByIdsInternal(@org.springframework.web.bind.annotation.RequestBody java.util.List<Long> ids);

    @GetMapping("/api/users/internal/by-manager/{managerId}")
    java.util.List<UserResponse> getEmployeesByManagerInternal(@PathVariable("managerId") Long managerId);
}
