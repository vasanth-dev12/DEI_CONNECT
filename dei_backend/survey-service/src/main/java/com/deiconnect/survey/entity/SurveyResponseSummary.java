package com.deiconnect.survey.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.survey.enums.SummaryScope;
import com.deiconnect.survey.enums.SummaryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "survey_response_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_summary_survey_scope_value",
                columnNames = {"survey_id", "scope", "scope_value"}),
        indexes = @Index(name = "idx_summary_survey", columnList = "survey_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class SurveyResponseSummary extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private InclusionSurvey survey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SummaryScope scope;

    @Column(name = "scope_value", nullable = false, length = 120)
    private String scopeValue;

    @Column(name = "respondent_count", nullable = false)
    private Integer respondentCount;

    @Column(name = "inclusion_index")
    private Double inclusionIndex;

    @Column(name = "key_theme_sentiment", length = 500)
    private String keyThemeSentiment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SummaryStatus status;
}
