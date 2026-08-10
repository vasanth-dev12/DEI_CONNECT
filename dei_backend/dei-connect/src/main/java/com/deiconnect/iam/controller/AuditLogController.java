package com.deiconnect.iam.controller;

import com.deiconnect.iam.dto.AuditLogResponse;
import com.deiconnect.iam.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> list(@RequestParam(required = false) Long userId,
                                                       @RequestParam(required = false) String entityType,
                                                       @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(auditLogService.find(userId, entityType, pageable));
    }
}
