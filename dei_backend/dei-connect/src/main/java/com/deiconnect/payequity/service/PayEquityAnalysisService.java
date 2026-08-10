package com.deiconnect.payequity.service;

import com.deiconnect.payequity.dto.PayEquityAnalysisRequest;
import com.deiconnect.payequity.dto.PayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PayGapFlagResponse;
import com.deiconnect.payequity.dto.PublishedPayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PublishedPayGapFlagResponse;
import com.deiconnect.payequity.dto.UpdatePayGapFlagRequest;
import com.deiconnect.payequity.enums.AnalysisStatus;
import com.deiconnect.payequity.enums.PayDimension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PayEquityAnalysisService {

    PayEquityAnalysisResponse createAnalysis(PayEquityAnalysisRequest request);

    PayEquityAnalysisResponse updateAnalysis(Long id, PayEquityAnalysisRequest request);

    PayEquityAnalysisResponse publishAnalysis(Long id);

    PayEquityAnalysisResponse getAnalysisById(Long id);

    Page<PayEquityAnalysisResponse> listAnalyses(PayDimension dimension, AnalysisStatus status, Long hrIdFilter, Pageable pageable);
    PayGapFlagResponse updateFlag(Long analysisId, Long flagId, UpdatePayGapFlagRequest request);

    PayEquityAnalysisResponse computeFromWorkforce(Long id);

    List<PayGapFlagResponse> listFlags(Long analysisId);

    Page<PublishedPayEquityAnalysisResponse> listPublishedAnalyses(Pageable pageable);

    PublishedPayEquityAnalysisResponse getPublishedAnalysisById(Long id);

    List<PublishedPayGapFlagResponse> listPublishedFlags(Long analysisId);
}
