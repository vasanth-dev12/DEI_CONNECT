package com.deiconnect.iam.dto;

import java.time.Instant;

public record AuditLogResponse(
        Long auditId,
        Long userId,
        String action,
        String entityType,
        Long recordId,
        Instant timestamp
) {
}
