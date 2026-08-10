package com.deiconnect.iam.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.iam.dto.AuditLogResponse;
import com.deiconnect.iam.entity.AuditLog;
import com.deiconnect.iam.mapper.AuditLogMapper;
import com.deiconnect.iam.repository.AuditLogRepository;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService implements AuditLogWriter {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

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
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .recordId(recordId)
                .timestamp(Instant.now())
                .build();
        if (userId != null) {
            log.setUser(userRepository.getReferenceById(userId));
        }
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> find(Long userId, String entityType, Pageable pageable) {
        boolean hasUser = userId != null;
        boolean hasType = StringUtils.hasText(entityType);

        Page<AuditLog> page;
        if (hasUser && hasType) {
            page = auditLogRepository.findByUser_IdAndEntityType(userId, entityType, pageable);
        } else if (hasUser) {
            page = auditLogRepository.findByUser_Id(userId, pageable);
        } else if (hasType) {
            page = auditLogRepository.findByEntityType(entityType, pageable);
        } else {
            page = auditLogRepository.findAll(pageable);
        }
        return page.map(auditLogMapper::toResponse);
    }
}
