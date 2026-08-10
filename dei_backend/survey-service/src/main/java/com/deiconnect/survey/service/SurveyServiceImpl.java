package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.CreateSurveyRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.dto.UpdateSurveyRequest;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.mapper.SurveyMapper;
import com.deiconnect.survey.mapper.SurveyQuestionMapper;
import com.deiconnect.survey.repository.InclusionSurveyRepository;
import com.deiconnect.survey.repository.SurveyParticipationRepository;
import com.deiconnect.survey.repository.SurveyResponseSummaryRepository;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.survey.client.EmitNotificationRequest;
import com.deiconnect.survey.client.NotificationClient;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    private final InclusionSurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final SurveyQuestionMapper questionMapper;
    private final AuditLogWriter auditLogWriter;
    private final UserClient userClient;
    private final SurveyResponseSummaryRepository summaryRepository;
    private final SurveyParticipationRepository participationRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public SurveyResponse create(CreateSurveyRequest request) {
        Long creatorManagerId = null;
        if (SecurityUtils.getCurrentRole() == Role.DEI_MANAGER) {
            creatorManagerId = SecurityUtils.getCurrentUserId();
        }

        if (creatorManagerId == null && request.questions() != null && !request.questions().isEmpty()) {
            throw new ConflictException("A survey template is created without questions — "
                    + "DEI managers add the questions for their own employees afterwards");
        }

        InclusionSurvey survey = InclusionSurvey.builder()
                .surveyName(request.surveyName())
                .surveyType(request.surveyType())
                .launchDate(request.launchDate())
                .closeDate(request.closeDate())
                .anonymised(request.anonymised() == null ? Boolean.TRUE : request.anonymised())
                .minResponseThreshold(request.minResponseThreshold())
                .status(SurveyStatus.DRAFT)
                .creatorManagerId(creatorManagerId)
                .build();

        if (request.questions() != null) {
            int nextOrder = 1;
            for (CreateQuestionRequest q : request.questions()) {
                SurveyQuestion question = questionMapper.toEntity(q);
                question.setCreatorManagerId(creatorManagerId);
                question.setSequenceOrder(q.sequenceOrder() != null ? q.sequenceOrder() : nextOrder);
                nextOrder = question.getSequenceOrder() + 1;
                survey.addQuestion(question);
            }
        }

        survey = surveyRepository.save(survey);
        auditLogWriter.record("CREATE_SURVEY", "InclusionSurvey", survey.getId());
        return surveyMapper.toResponseWithQuestions(survey);
    }

    @Override
    @Transactional
    public SurveyResponse update(Long id, UpdateSurveyRequest request) {
        InclusionSurvey survey = findOrThrow(id);
        requireManageable(survey);
        requireStatus(survey, SurveyStatus.DRAFT, "update");
        survey.setSurveyName(request.surveyName());
        survey.setSurveyType(request.surveyType());
        survey.setLaunchDate(request.launchDate());
        survey.setCloseDate(request.closeDate());
        survey.setAnonymised(request.anonymised() == null ? survey.getAnonymised() : request.anonymised());
        survey.setMinResponseThreshold(request.minResponseThreshold());
        survey = surveyRepository.save(survey);
        auditLogWriter.record("UPDATE_SURVEY", "InclusionSurvey", survey.getId());
        return surveyMapper.toResponseWithQuestions(survey);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        InclusionSurvey survey = findOrThrow(id);
        requireManageable(survey);
        requireStatus(survey, SurveyStatus.DRAFT, "delete");
        surveyRepository.delete(survey);
        auditLogWriter.record("DELETE_SURVEY", "InclusionSurvey", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SurveyResponse> list(SurveyStatus status, Pageable pageable) {
        Role callerRole = SecurityUtils.getCurrentRole();
        Long callerUserId = SecurityUtils.getCurrentUserId();

        if (callerRole == Role.EMPLOYEE) {
            UserResponse employee = userClient.getByIdInternal(callerUserId);
            Long managerId = employee.managerId();
            Page<InclusionSurvey> page = surveyRepository.findAnswerableByEmployee(
                    SurveyStatus.ACTIVE, managerId, pageable);
            return page.map(survey -> surveyMapper.toResponse(survey, hasResponded(survey.getId(), callerUserId)));
        }

        if (callerRole == Role.DEI_MANAGER) {
            Page<InclusionSurvey> page = (status == null)
                    ? surveyRepository.findVisibleToManager(callerUserId, pageable)
                    : surveyRepository.findVisibleToManagerByStatus(status, callerUserId, pageable);
            return page.map(surveyMapper::toResponse);
        }

        Page<InclusionSurvey> page = (status == null)
                ? surveyRepository.findAll(pageable)
                : surveyRepository.findByStatus(status, pageable);
        return page.map(surveyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResponse getById(Long id) {
        InclusionSurvey survey = findOrThrow(id);
        Role callerRole = SecurityUtils.getCurrentRole();
        Long callerUserId = SecurityUtils.getCurrentUserId();

        Long employeeManagerId = null;

        if (callerRole == Role.EMPLOYEE) {
            if (survey.getStatus() != SurveyStatus.ACTIVE) {
                throw new ResourceNotFoundException("InclusionSurvey", id);
            }
            UserResponse employee = userClient.getByIdInternal(callerUserId);
            employeeManagerId = employee.managerId();
            if (survey.getCreatorManagerId() != null) {
                if (employeeManagerId == null
                        || !survey.getCreatorManagerId().equals(employeeManagerId)) {
                    throw new ResourceNotFoundException("InclusionSurvey", id);
                }
            }
        }

        if (callerRole == Role.DEI_MANAGER) {
            if (survey.getCreatorManagerId() != null && !survey.getCreatorManagerId().equals(callerUserId)) {
                throw new ForbiddenOperationException("You may only access surveys you created");
            }
        }

        List<QuestionResponse> questions = scopedQuestions(survey, callerRole, callerUserId, employeeManagerId)
                .stream()
                .map(questionMapper::toResponse)
                .toList();
        if (callerRole == Role.EMPLOYEE && questions.isEmpty()) {
            throw new ResourceNotFoundException("InclusionSurvey", id);
        }

        Boolean respondedByMe = callerRole == Role.EMPLOYEE
                ? hasResponded(id, callerUserId)
                : null;
        return surveyMapper.toResponse(survey, questions, respondedByMe);
    }

    private boolean hasResponded(Long surveyId, Long employeeUserId) {
        return employeeUserId != null
                && participationRepository.existsBySurvey_IdAndEmployeeUserId(surveyId, employeeUserId);
    }

    private List<SurveyQuestion> scopedQuestions(InclusionSurvey survey, Role role,
                                                 Long callerId, Long employeeManagerId) {
        Comparator<SurveyQuestion> bySequence = Comparator.comparing(SurveyQuestion::getSequenceOrder);
        if (role == Role.DEI_MANAGER) {
            return survey.getQuestions().stream()
                    .filter(q -> q.getCreatorManagerId() == null || q.getCreatorManagerId().equals(callerId))
                    .sorted(bySequence)
                    .toList();
        }
        if (role == Role.EMPLOYEE) {
            return survey.getQuestions().stream()
                    .filter(q -> q.getCreatorManagerId() == null
                            || (employeeManagerId != null && q.getCreatorManagerId().equals(employeeManagerId)))
                    .sorted(bySequence)
                    .toList();
        }
        return survey.getQuestions().stream().sorted(bySequence).toList();
    }

    @Override
    @Transactional
    public SurveyResponse launch(Long id) {
        InclusionSurvey survey = findOrThrow(id);
        requireManageable(survey);
        requireStatus(survey, SurveyStatus.DRAFT, "launch");
        if (survey.getQuestions().isEmpty()) {
            throw new ConflictException("Cannot launch a survey with no questions");
        }
        survey.setStatus(SurveyStatus.ACTIVE);
        survey = surveyRepository.save(survey);
        auditLogWriter.record("LAUNCH_SURVEY", "InclusionSurvey", survey.getId());
        notifyAudience(survey);
        return surveyMapper.toResponse(survey);
    }

    private void notifyAudience(InclusionSurvey survey) {
        try {
            Set<Long> managerIds = new LinkedHashSet<>();
            if (survey.getCreatorManagerId() != null) {
                managerIds.add(survey.getCreatorManagerId());
            }
            survey.getQuestions().stream()
                    .map(SurveyQuestion::getCreatorManagerId)
                    .filter(Objects::nonNull)
                    .forEach(managerIds::add);

            if (managerIds.isEmpty()) {
                log.info("Survey {} launched with no manager-scoped questions — no team to notify.",
                        survey.getId());
                return;
            }

            String message = "A new survey is open for you: " + survey.getSurveyName();
            int notified = 0;
            for (Long managerId : managerIds) {
                for (UserResponse employee : userClient.getEmployeesByManagerInternal(managerId)) {
                    if (employee.employeeId() == null) {
                        continue;
                    }
                    notificationClient.emitInternal(new EmitNotificationRequest(
                            employee.employeeId(),
                            EmitNotificationRequest.NotificationCategory.SURVEY,
                            message));
                    notified++;
                }
            }
            log.info("Survey {} launched: notified {} employee(s) across {} manager team(s).",
                    survey.getId(), notified, managerIds.size());
        } catch (Exception ex) {
            log.error("Survey {} launched, but notifying its audience failed: {}",
                    survey.getId(), ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public SurveyResponse close(Long id) {
        InclusionSurvey survey = findOrThrow(id);
        requireManageable(survey);
        requireStatus(survey, SurveyStatus.ACTIVE, "close");
        survey.setStatus(SurveyStatus.CLOSED);
        survey = surveyRepository.save(survey);
        auditLogWriter.record("CLOSE_SURVEY", "InclusionSurvey", survey.getId());
        return surveyMapper.toResponse(survey);
    }

    @Override
    @Transactional
    public SurveyResponse publishResults(Long id) {
        InclusionSurvey survey = findOrThrow(id);
        requireManageable(survey);
        requireStatus(survey, SurveyStatus.CLOSED, "publish results for");
        survey.setStatus(SurveyStatus.PUBLISHED);
        survey = surveyRepository.save(survey);
        auditLogWriter.record("PUBLISH_SURVEY", "InclusionSurvey", survey.getId());
        return surveyMapper.toResponse(survey);
    }

    @Override
    public InclusionSurvey findOrThrow(Long id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InclusionSurvey", id));
    }

    private void requireStatus(InclusionSurvey survey, SurveyStatus required, String action) {
        if (survey.getStatus() != required) {
            throw new ConflictException("Cannot " + action + " a survey in status " + survey.getStatus()
                    + " (required: " + required + ")");
        }
    }

    private void requireManageable(InclusionSurvey survey) {
        if (SecurityUtils.getCurrentRole() != Role.DEI_MANAGER) {
            return;
        }
        Long me = SecurityUtils.getCurrentUserId();
        if (survey.getCreatorManagerId() == null || !survey.getCreatorManagerId().equals(me)) {
            throw new ForbiddenOperationException("You may only manage surveys you created");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageInclusionIndex(String scope, String scopeValue) {
        if (scope != null && !scope.isBlank()) {
            try {
                com.deiconnect.survey.enums.SummaryScope summaryScope = com.deiconnect.survey.enums.SummaryScope.valueOf(scope.toUpperCase());
                return summaryRepository.avgPublishedInclusionIndexByScope(summaryScope, scopeValue);
            } catch (IllegalArgumentException e) {
                return 0.0;
            }
        }
        return summaryRepository.avgPublishedInclusionIndex();
    }
}
