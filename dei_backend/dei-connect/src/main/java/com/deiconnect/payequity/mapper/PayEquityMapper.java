package com.deiconnect.payequity.mapper;

import com.deiconnect.payequity.dto.PayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PayGapFlagResponse;
import com.deiconnect.payequity.dto.PublishedPayEquityAnalysisResponse;
import com.deiconnect.payequity.dto.PublishedPayGapFlagResponse;
import com.deiconnect.payequity.entity.PayEquityAnalysis;
import com.deiconnect.payequity.entity.PayGapFlag;
import org.springframework.stereotype.Component;

@Component
public class PayEquityMapper {

    public PayEquityAnalysisResponse toResponse(PayEquityAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        return new PayEquityAnalysisResponse(
                analysis.getId(),
                analysis.getAnalysisPeriod(),
                analysis.getDimension(),
                analysis.getControlVariables(),
                analysis.getMedianGapPercent(),
                analysis.getAdjustedGapPercent(),
                analysis.getSignificanceLevel(),
                analysis.getRunBy() != null ? analysis.getRunBy().getId() : null,
                analysis.getRunBy() != null ? analysis.getRunBy().getName() : null,
                analysis.getStatus(),
                analysis.getCreatedDate(),
                analysis.getLastModifiedDate()
        );
    }

    public PublishedPayEquityAnalysisResponse toPublishedResponse(PayEquityAnalysis analysis) {
        if (analysis == null) {
            return null;
        }
        return new PublishedPayEquityAnalysisResponse(
                analysis.getId(),
                analysis.getAnalysisPeriod(),
                analysis.getDimension(),
                analysis.getControlVariables(),
                analysis.getMedianGapPercent(),
                analysis.getAdjustedGapPercent(),
                analysis.getSignificanceLevel(),
                analysis.getStatus(),
                analysis.getCreatedDate(),
                analysis.getLastModifiedDate()
        );
    }

    public PayGapFlagResponse toResponse(PayGapFlag flag) {
        if (flag == null) {
            return null;
        }
        return new PayGapFlagResponse(
                flag.getId(),
                flag.getAnalysis() != null ? flag.getAnalysis().getId() : null,
                flag.getDepartmentId(),
                flag.getGradeId(),
                flag.getGroupName(),
                flag.getGapPercent(),
                flag.getAffectedEmployeeCount(),
                flag.getRemediationOwner() != null ? flag.getRemediationOwner().getId() : null,
                flag.getRemediationOwner() != null ? flag.getRemediationOwner().getName() : null,
                flag.getStatus(),
                flag.getCreatedDate(),
                flag.getLastModifiedDate()
        );
    }

    public PublishedPayGapFlagResponse toPublishedResponse(PayGapFlag flag, int minGroupSize) {
        if (flag == null) {
            return null;
        }
        boolean suppressed = flag.getAffectedEmployeeCount() == null || flag.getAffectedEmployeeCount() < minGroupSize;
        return new PublishedPayGapFlagResponse(
                flag.getId(),
                flag.getAnalysis() != null ? flag.getAnalysis().getId() : null,
                suppressed ? null : flag.getDepartmentId(),
                suppressed ? null : flag.getGradeId(),
                suppressed ? null : flag.getGroupName(),
                suppressed ? null : flag.getGapPercent(),
                suppressed ? null : flag.getAffectedEmployeeCount(),
                flag.getStatus(),
                suppressed
        );
    }
}
