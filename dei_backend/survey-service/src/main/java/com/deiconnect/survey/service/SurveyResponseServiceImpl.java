package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.PrivacyThresholdViolationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.common.enums.Role;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import com.deiconnect.survey.dto.AnswerItem;
import com.deiconnect.survey.dto.SubmitAcknowledgement;
import com.deiconnect.survey.dto.SubmitSurveyRequest;
import com.deiconnect.survey.dto.SummaryResponse;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyParticipation;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.entity.SurveyResponseSummary;
import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SummaryScope;
import com.deiconnect.survey.enums.SummaryStatus;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.mapper.SurveyResponseSummaryMapper;
import com.deiconnect.survey.repository.SurveyParticipationRepository;
import com.deiconnect.survey.repository.SurveyQuestionRepository;
import com.deiconnect.survey.repository.SurveyResponseSummaryRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SurveyResponseServiceImpl implements SurveyResponseService {

    private final SurveyService surveyService;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyResponseSummaryRepository summaryRepository;
    private final SurveyParticipationRepository participationRepository;
    private final SurveyResponseSummaryMapper summaryMapper;
    private final UserClient userClient;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public SubmitAcknowledgement submit(Long surveyId, SubmitSurveyRequest request) {
        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        InclusionSurvey survey = surveyService.findOrThrow(surveyId);

        validateOpenForSubmission(survey);

        if (participationRepository.existsBySurvey_IdAndEmployeeUserId(surveyId, principal.getId())) {
            throw new ConflictException("You have already submitted this survey");
        }

        UserResponse user = userClient.getByIdInternal(principal.getId());

        Map<Long, SurveyQuestion> questions = loadQuestions(surveyId, user);
        double submissionScore = scoreSubmission(survey, questions, request.answers());
        if (user.departmentId() != null) {
            updateRunningSummary(survey, SummaryScope.DEPARTMENT,
                    String.valueOf(user.departmentId()), submissionScore);
        }
        if (user.gradeId() != null) {
            updateRunningSummary(survey, SummaryScope.GRADE,
                    String.valueOf(user.gradeId()), submissionScore);
        }
        if (user.managerId() != null) {
            updateRunningSummary(survey, SummaryScope.MANAGER,
                    String.valueOf(user.managerId()), submissionScore);
        }
        if (user.hrId() != null) {
            updateRunningSummary(survey, SummaryScope.HR,
                    String.valueOf(user.hrId()), submissionScore);
        }

        participationRepository.save(SurveyParticipation.builder()
                .survey(survey)
                .employeeUserId(principal.getId())
                .build());

        auditLogWriter.record(principal.getId(), "SUBMIT_SURVEY", "InclusionSurvey", surveyId);

        return new SubmitAcknowledgement(surveyId, true,
                "Your anonymous response has been recorded. Thank you for participating.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SummaryResponse> getSummaries(Long surveyId, Pageable pageable) {
        InclusionSurvey survey = surveyService.findOrThrow(surveyId);
        int threshold = survey.getMinResponseThreshold();
        auditLogWriter.record("VIEW_SUMMARIES", "SurveyResponseSummary", surveyId);

        if (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER) {
            Long managerId = SecurityUtils.getCurrentUserId();
            java.util.Optional<SurveyResponseSummary> optSummary = summaryRepository
                    .findBySurvey_IdAndScopeAndScopeValue(surveyId, SummaryScope.MANAGER, String.valueOf(managerId));
            if (optSummary.isEmpty()) {
                return org.springframework.data.domain.Page.empty(pageable);
            }
            List<SummaryResponse> content = List.of(summaryMapper.toResponse(optSummary.get(), threshold));
            return new org.springframework.data.domain.PageImpl<>(content, pageable, 1);
        }

        return summaryRepository.findBySurvey_Id(surveyId, pageable)
                .map(s -> summaryMapper.toResponse(s, threshold));
    }

    @Override
    @Transactional
    public SummaryResponse publishSummary(Long surveyId, Long summaryId) {
        SurveyResponseSummary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyResponseSummary", summaryId));
        if (!summary.getSurvey().getId().equals(surveyId)) {
            throw new ForbiddenOperationException("Summary does not belong to survey " + surveyId);
        }
        if (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER) {
            Long managerId = SecurityUtils.getCurrentUserId();
            boolean ownTeamSummary = summary.getScope() == SummaryScope.MANAGER
                    && String.valueOf(managerId).equals(summary.getScopeValue());
            if (!ownTeamSummary) {
                throw new ForbiddenOperationException(
                        "You may only publish the summary for your own team");
            }
        }
        int threshold = summary.getSurvey().getMinResponseThreshold();

        if (summary.getRespondentCount() == null || summary.getRespondentCount() < threshold) {
            throw new PrivacyThresholdViolationException(
                    "Cannot publish a summary below the minimum response threshold");
        }
        summary.setStatus(SummaryStatus.PUBLISHED);
        summary = summaryRepository.save(summary);
        auditLogWriter.record("PUBLISH_SUMMARY", "SurveyResponseSummary", summary.getId());
        return summaryMapper.toResponse(summary, threshold);
    }

    private void validateOpenForSubmission(InclusionSurvey survey) {
        if (survey.getStatus() != SurveyStatus.ACTIVE) {
            throw new ConflictException("Survey is not open for responses (status: "
                    + survey.getStatus() + ")");
        }
        LocalDate today = LocalDate.now();
        if (survey.getLaunchDate() != null && today.isBefore(survey.getLaunchDate())) {
            throw new ConflictException("Survey has not launched yet");
        }
        if (survey.getCloseDate() != null && today.isAfter(survey.getCloseDate())) {
            throw new ConflictException("Survey response window has closed");
        }
    }

    private Map<Long, SurveyQuestion> loadQuestions(Long surveyId, UserResponse employee) {
        Map<Long, SurveyQuestion> map = new HashMap<>();
        List<SurveyQuestion> questions = questionRepository
                .findVisibleQuestions(surveyId, employee.managerId());
        for (SurveyQuestion q : questions) {
            map.put(q.getId(), q);
        }
        return map;
    }

    private double scoreSubmission(InclusionSurvey survey, Map<Long, SurveyQuestion> questions,
                                   List<AnswerItem> answers) {
        Map<Long, AnswerItem> answered = new HashMap<>();
        for (AnswerItem a : answers) {
            SurveyQuestion question = questions.get(a.questionId());
            if (question == null) {
                throw new ForbiddenOperationException(
                        "Answer references a question not in this survey: " + a.questionId());
            }
            answered.put(a.questionId(), a);
        }

        double sum = 0.0;
        int numericCount = 0;

        for (SurveyQuestion question : questions.values()) {
            AnswerItem answer = answered.get(question.getId());
            boolean hasValue = answer != null && answer.numericValue() != null;

            if (Boolean.TRUE.equals(question.getMandatory()) && !hasValue) {
                throw new ConflictException("Mandatory question not answered: " + question.getId());
            }
            if (!hasValue) {
                continue;
            }

            sum += normalise(question.getQuestionType(), answer.numericValue());
            numericCount++;
        }

        return numericCount == 0 ? 0.0 : round(sum / numericCount);
    }

    private double normalise(QuestionType type, Integer value) {
        return switch (type) {
            case LIKERT_SCALE -> {
                if (value < 1 || value > 5) {
                    throw new ConflictException("Likert answer must be between 1 and 5");
                }
                yield ((value - 1) / 4.0) * 100.0;
            }
            case BINARY -> {
                if (value != 0 && value != 1) {
                    throw new ConflictException("Binary answer must be 0 or 1");
                }
                yield value * 100.0;
            }
        };
    }

    private void updateRunningSummary(InclusionSurvey survey, SummaryScope scope,
                                      String scopeValue, double submissionScore) {
        SurveyResponseSummary summary = summaryRepository
                .findBySurvey_IdAndScopeAndScopeValue(survey.getId(), scope, scopeValue)
                .orElseGet(() -> SurveyResponseSummary.builder()
                        .survey(survey)
                        .scope(scope)
                        .scopeValue(scopeValue)
                        .respondentCount(0)
                        .inclusionIndex(0.0)
                        .status(SummaryStatus.COMPUTED)
                        .build());

        int oldCount = summary.getRespondentCount() == null ? 0 : summary.getRespondentCount();
        double oldIndex = summary.getInclusionIndex() == null ? 0.0 : summary.getInclusionIndex();
        int newCount = oldCount + 1;
        double newIndex = round(((oldIndex * oldCount) + submissionScore) / newCount);

        summary.setRespondentCount(newCount);
        summary.setInclusionIndex(newIndex);
        summaryRepository.save(summary);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
