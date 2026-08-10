package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import com.deiconnect.survey.dto.AnswerItem;
import com.deiconnect.survey.dto.SubmitAcknowledgement;
import com.deiconnect.survey.dto.SubmitSurveyRequest;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyParticipation;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.entity.SurveyResponseSummary;
import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.mapper.SurveyResponseSummaryMapper;
import com.deiconnect.survey.repository.SurveyParticipationRepository;
import com.deiconnect.survey.repository.SurveyQuestionRepository;
import com.deiconnect.survey.repository.SurveyResponseSummaryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceTest {

    @Mock private SurveyService surveyService;
    @Mock private SurveyQuestionRepository questionRepository;
    @Mock private SurveyResponseSummaryRepository summaryRepository;
    @Mock private SurveyParticipationRepository participationRepository;
    @Mock private SurveyResponseSummaryMapper summaryMapper;
    @Mock private UserClient userClient;
    @Mock private AuditLogWriter auditLogWriter;

    private SurveyResponseService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new SurveyResponseServiceImpl(surveyService, questionRepository, summaryRepository, participationRepository, summaryMapper, userClient, auditLogWriter);
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(Long id, Role role) {
        DeiUserPrincipal principal = new DeiUserPrincipal(id, "EMP100", "test@test.com", "pass", role, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void submit_ThrowsConflict_WhenAlreadySubmitted() {
        mockAuthentication(100L, Role.EMPLOYEE);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.ACTIVE);
        survey.setCloseDate(LocalDate.now().plusDays(2));

        when(surveyService.findOrThrow(1L)).thenReturn(survey);
        when(participationRepository.existsBySurvey_IdAndEmployeeUserId(1L, 100L)).thenReturn(true);

        SubmitSurveyRequest request = new SubmitSurveyRequest(List.of());

        assertThrows(ConflictException.class, () -> service.submit(1L, request));
    }

    private InclusionSurvey openSurveyWith(SurveyQuestion... questions) {
        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.ACTIVE);
        survey.setCloseDate(LocalDate.now().plusDays(2));
        when(surveyService.findOrThrow(1L)).thenReturn(survey);
        when(participationRepository.existsBySurvey_IdAndEmployeeUserId(1L, 100L)).thenReturn(false);
        when(userClient.getByIdInternal(100L)).thenReturn(new UserResponse(
                100L, "EMP100", "Employee", "e@x.io", Role.EMPLOYEE, 10L, 20L, "ACTIVE",
                7L, "Mgr", 20L, "HR", null, null));
        when(questionRepository.findVisibleQuestions(1L, 7L)).thenReturn(List.of(questions));
        return survey;
    }

    private static SurveyQuestion question(Long id, QuestionType type, boolean mandatory) {
        SurveyQuestion q = new SurveyQuestion();
        q.setId(id);
        q.setQuestionType(type);
        q.setMandatory(mandatory);
        q.setSequenceOrder(1);
        return q;
    }

    private Double capturedIndex() {
        ArgumentCaptor<SurveyResponseSummary> saved = ArgumentCaptor.forClass(SurveyResponseSummary.class);
        verify(summaryRepository, atLeastOnce()).save(saved.capture());
        return saved.getAllValues().get(0).getInclusionIndex();
    }

    @Test
    void submit_ScoresLikertAnswerOnZeroToHundredScale() {
        openSurveyWith(question(5L, QuestionType.LIKERT_SCALE, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        SubmitAcknowledgement ack = service.submit(1L,
                new SubmitSurveyRequest(List.of(new AnswerItem(5L, 4))));

        assertTrue(ack.accepted());
        assertEquals(75.0, capturedIndex());
        verify(participationRepository).save(any(SurveyParticipation.class));
    }

    @Test
    void submit_ScoresYesNoAnswers() {
        openSurveyWith(question(6L, QuestionType.BINARY, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        service.submit(1L, new SubmitSurveyRequest(List.of(new AnswerItem(6L, 1))));

        assertEquals(100.0, capturedIndex(), "yes -> 100");
    }

    @Test
    void submit_ScoresNoAsZero_NotAsMissing() {
        openSurveyWith(question(6L, QuestionType.BINARY, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        service.submit(1L, new SubmitSurveyRequest(List.of(new AnswerItem(6L, 0))));

        assertEquals(0.0, capturedIndex(), "no -> 0");
    }

    @Test
    void submit_AveragesMixedLikertAndYesNo() {
        openSurveyWith(question(5L, QuestionType.LIKERT_SCALE, true),
                       question(6L, QuestionType.BINARY, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        service.submit(1L, new SubmitSurveyRequest(List.of(
                new AnswerItem(5L, 5),
                new AnswerItem(6L, 0))));

        assertEquals(50.0, capturedIndex(), "mean of 100 and 0");
    }

    @Test
    void submit_RejectsOutOfRangeLikert() {
        openSurveyWith(question(5L, QuestionType.LIKERT_SCALE, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        assertThrows(ConflictException.class, () -> service.submit(1L,
                new SubmitSurveyRequest(List.of(new AnswerItem(5L, 6)))));
    }

    @Test
    void submit_RejectsOutOfRangeBinary() {
        openSurveyWith(question(6L, QuestionType.BINARY, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        assertThrows(ConflictException.class, () -> service.submit(1L,
                new SubmitSurveyRequest(List.of(new AnswerItem(6L, 2)))));
    }

    @Test
    void submit_RejectsMissingMandatoryAnswer() {
        openSurveyWith(question(5L, QuestionType.LIKERT_SCALE, true));
        mockAuthentication(100L, Role.EMPLOYEE);

        assertThrows(ConflictException.class,
                () -> service.submit(1L, new SubmitSurveyRequest(List.of())));
    }

    @Test
    void submit_RejectsAnswerToAQuestionTheEmployeeCannotSee() {
        openSurveyWith(question(5L, QuestionType.LIKERT_SCALE, false));
        mockAuthentication(100L, Role.EMPLOYEE);

        assertThrows(ForbiddenOperationException.class, () -> service.submit(1L,
                new SubmitSurveyRequest(List.of(new AnswerItem(999L, 3)))));
    }
}
