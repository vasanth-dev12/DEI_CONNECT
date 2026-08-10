package com.deiconnect.iam.mapper;

import com.deiconnect.iam.dto.AuditLogResponse;
import com.deiconnect.iam.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        Long userId = log.getUser() == null ? null : log.getUser().getId();
        return new AuditLogResponse(
                log.getId(),
                userId,
                log.getAction(),
                log.getEntityType(),
                log.getRecordId(),
                log.getTimestamp());
    }
}
