package com.deiconnect.reporting.repository;

import com.deiconnect.reporting.entity.DEIReport;
import com.deiconnect.reporting.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DEIReportRepository extends JpaRepository<DEIReport, Long> {

    Page<DEIReport> findByStatus(ReportStatus status, Pageable pageable);

    Page<DEIReport> findByCreatedBy_Id(Long createdById, Pageable pageable);
}
