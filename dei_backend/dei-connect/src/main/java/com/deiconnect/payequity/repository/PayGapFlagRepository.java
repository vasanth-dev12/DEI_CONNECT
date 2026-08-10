package com.deiconnect.payequity.repository;

import com.deiconnect.payequity.entity.PayGapFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayGapFlagRepository extends JpaRepository<PayGapFlag, Long> {

    List<PayGapFlag> findByAnalysisId(Long analysisId);
}
