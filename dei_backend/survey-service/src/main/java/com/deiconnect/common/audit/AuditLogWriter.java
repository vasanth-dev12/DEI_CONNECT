package com.deiconnect.common.audit;

public interface AuditLogWriter {

    void record(String action, String entityType, Long recordId);

    void record(Long userId, String action, String entityType, Long recordId);
}
