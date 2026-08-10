package com.deiconnect.common.audit;

import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuditLogWriterImpl implements AuditLogWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogWriterImpl.class);

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, Long recordId) {
        persist(SecurityUtils.getCurrentUserId(), action, entityType, recordId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String action, String entityType, Long recordId) {
        persist(userId, action, entityType, recordId);
    }

    private void persist(Long userId, String action, String entityType, Long recordId) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .recordId(recordId)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(entry);
        log.debug("AUDIT LOG persisted: userId={}, action={}, entityType={}, recordId={}",
                userId, action, entityType, recordId);
    }
}
