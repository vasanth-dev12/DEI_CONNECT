package com.deiconnect.payequity.repository;

import com.deiconnect.payequity.entity.PayEquityAnalysis;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.PayDimension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayEquityAnalysisRepository extends JpaRepository<PayEquityAnalysis, Long> {

    @Query("select a from PayEquityAnalysis a where "
            + "(:dimension is null or a.dimension = :dimension) and "
            + "(:status is null or a.status = :status) and "
            + "(:runById is null or a.runBy.id = :runById)")
    Page<PayEquityAnalysis> search(@Param("dimension") PayDimension dimension,
                                   @Param("status") AnalysisStatus status,
                                   @Param("runById") Long runById,
                                   Pageable pageable);
}
