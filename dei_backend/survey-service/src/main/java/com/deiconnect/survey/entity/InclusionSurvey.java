package com.deiconnect.survey.entity;

import com.deiconnect.common.entity.BaseAuditEntity;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.enums.SurveyType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inclusion_survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class InclusionSurvey extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "survey_name", nullable = false, length = 200)
    private String surveyName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "survey_type", nullable = false, length = 30)
    private SurveyType surveyType;

    @Column(name = "launch_date")
    private LocalDate launchDate;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(nullable = false)
    private Boolean anonymised;

    @Column(name = "min_response_threshold", nullable = false)
    private Integer minResponseThreshold;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private SurveyStatus status;

    @Column(name = "creator_manager_user_id")
    private Long creatorManagerId;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<SurveyQuestion> questions = new ArrayList<>();

    public void addQuestion(SurveyQuestion question) {
        question.setSurvey(this);
        this.questions.add(question);
    }

    public void removeQuestion(SurveyQuestion question) {
        this.questions.remove(question);
        question.setSurvey(null);
    }
}
