package com.deiconnect.survey.repository;

import com.deiconnect.survey.entity.SurveyResponseSummary;
import com.deiconnect.survey.enums.SummaryScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SurveyResponseSummaryRepository extends JpaRepository<SurveyResponseSummary, Long> {

    Optional<SurveyResponseSummary> findBySurvey_IdAndScopeAndScopeValue(
            Long surveyId, SummaryScope scope, String scopeValue);

    Page<SurveyResponseSummary> findBySurvey_Id(Long surveyId, Pageable pageable);

    @Query("select avg(s.inclusionIndex) from SurveyResponseSummary s "
            + "where s.status = com.deiconnect.survey.enums.SummaryStatus.PUBLISHED")
    Double avgPublishedInclusionIndex();

    @Query("select avg(s.inclusionIndex) from SurveyResponseSummary s "
            + "where s.status = com.deiconnect.survey.enums.SummaryStatus.PUBLISHED "
            + "and s.scope = :scope and s.scopeValue = :scopeValue")
    Double avgPublishedInclusionIndexByScope(@Param("scope") SummaryScope scope,
                                             @Param("scopeValue") String scopeValue);
}
