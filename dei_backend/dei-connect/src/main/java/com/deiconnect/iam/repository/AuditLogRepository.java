package com.deiconnect.iam.repository;

import com.deiconnect.iam.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUser_Id(Long userId, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByUser_IdAndEntityType(Long userId, String entityType, Pageable pageable);
}
