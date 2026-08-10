package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.common.enums.Role;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.dto.UpdateQuestionRequest;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.mapper.SurveyQuestionMapper;
import com.deiconnect.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyQuestionServiceImpl implements SurveyQuestionService {

    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionMapper questionMapper;
    private final SurveyService surveyService;
    private final UserClient userClient;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> listForSurvey(Long surveyId, Long managerIdFilter) {
        surveyService.getById(surveyId);

        Role role = SecurityUtils.getCurrentRole();
        Long callerId = SecurityUtils.getCurrentUserId();

        List<SurveyQuestion> questions;
        if (role == Role.DEI_MANAGER) {
            questions = questionRepository.findVisibleQuestions(surveyId, callerId);
        } else if (role == Role.EMPLOYEE) {
            Long managerId = userClient.getByIdInternal(callerId).managerId();
            questions = questionRepository.findVisibleQuestions(surveyId, managerId);
        } else {
            questions = questionRepository.findBySurvey_IdOrderBySequenceOrderAsc(surveyId);
        }

        return questions.stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuestionResponse add(Long surveyId, CreateQuestionRequest request) {
        InclusionSurvey survey = surveyService.findOrThrow(surveyId);
        requireDraft(survey);

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();

        SurveyQuestion question = questionMapper.toEntity(request);
        question.setSurvey(survey);

        question.setCreatorManagerId(principal.getRole() == Role.DEI_MANAGER ? principal.getId() : null);
        question.setSequenceOrder(request.sequenceOrder() != null
                ? request.sequenceOrder()
                : nextSequenceOrder(surveyId));

        question = questionRepository.save(question);
        auditLogWriter.record("ADD_QUESTION", "SurveyQuestion", question.getId());
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional
    public QuestionResponse update(Long surveyId, Long questionId, UpdateQuestionRequest request) {
        SurveyQuestion question = loadInSurvey(surveyId, questionId);
        requireDraft(question.getSurvey());

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (question.getCreatorManagerId() == null || !question.getCreatorManagerId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You can only edit questions created by yourself");
            }
        }

        question.setQuestionText(request.questionText());
        question.setQuestionType(request.questionType());
        question.setDimension(request.dimension());
        question.setMandatory(request.mandatory());
        if (request.sequenceOrder() != null) {
            question.setSequenceOrder(request.sequenceOrder());
        }
        question = questionRepository.save(question);
        auditLogWriter.record("UPDATE_QUESTION", "SurveyQuestion", question.getId());
        return questionMapper.toResponse(question);
    }

    @Override
    @Transactional
    public void delete(Long surveyId, Long questionId) {
        SurveyQuestion question = loadInSurvey(surveyId, questionId);
        requireDraft(question.getSurvey());

        DeiUserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        if (principal.getRole() == Role.DEI_MANAGER) {
            if (question.getCreatorManagerId() == null || !question.getCreatorManagerId().equals(principal.getId())) {
                throw new ForbiddenOperationException("You can only delete questions created by yourself");
            }
        }

        questionRepository.delete(question);
        auditLogWriter.record("DELETE_QUESTION", "SurveyQuestion", questionId);
    }

    private SurveyQuestion loadInSurvey(Long surveyId, Long questionId) {
        SurveyQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyQuestion", questionId));
        if (!question.getSurvey().getId().equals(surveyId)) {
            throw new ForbiddenOperationException("Question does not belong to survey " + surveyId);
        }
        return question;
    }

    private int nextSequenceOrder(Long surveyId) {
        Integer max = questionRepository.findMaxSequenceOrder(surveyId);
        return max == null ? 1 : max + 1;
    }

    private void requireDraft(InclusionSurvey survey) {
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new ConflictException("Questions can only be modified while the survey is DRAFT "
                    + "(current: " + survey.getStatus() + ")");
        }
    }
}
