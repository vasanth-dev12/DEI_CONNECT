package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.survey.client.EmitNotificationRequest;
import com.deiconnect.survey.client.NotificationClient;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import com.deiconnect.survey.dto.CreateQuestionRequest;
import com.deiconnect.survey.dto.CreateSurveyRequest;
import com.deiconnect.survey.dto.SurveyResponse;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.enums.QuestionType;
import com.deiconnect.survey.enums.SurveyDimension;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.enums.SurveyType;
import com.deiconnect.survey.mapper.SurveyMapper;
import com.deiconnect.survey.mapper.SurveyQuestionMapper;
import com.deiconnect.survey.repository.InclusionSurveyRepository;
import com.deiconnect.survey.repository.SurveyParticipationRepository;
import com.deiconnect.survey.repository.SurveyResponseSummaryRepository;
import com.deiconnect.security.DeiUserPrincipal;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock private InclusionSurveyRepository surveyRepository;
    @Mock private SurveyMapper surveyMapper;
    @Mock private SurveyQuestionMapper questionMapper;
    @Mock private AuditLogWriter auditLogWriter;
    @Mock private UserClient userClient;
    @Mock private SurveyResponseSummaryRepository summaryRepository;
    @Mock private SurveyParticipationRepository participationRepository;
    @Mock private NotificationClient notificationClient;

    private SurveyService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new SurveyServiceImpl(surveyRepository, surveyMapper, questionMapper, auditLogWriter,
                userClient, summaryRepository, participationRepository, notificationClient);
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
    void list_AsEmployee_ReturnsOnlyAnswerableSurveysForTheirManager() {
        mockAuthentication(100L, Role.EMPLOYEE);

        UserResponse empProfile = new UserResponse(100L, "EMP100", "Employee Name", "emp@test.com", Role.EMPLOYEE, 10L, 20L, "ACTIVE", 10L, "Manager Name", 20L, "HR Name", Instant.now(), Instant.now());
        when(userClient.getByIdInternal(100L)).thenReturn(empProfile);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InclusionSurvey> expectedPage = new PageImpl<>(List.of(new InclusionSurvey()));
        when(surveyRepository.findAnswerableByEmployee(SurveyStatus.ACTIVE, 10L, pageable))
                .thenReturn(expectedPage);

        Page<SurveyResponse> actual = service.list(null, pageable);

        assertNotNull(actual);
        verify(surveyRepository).findAnswerableByEmployee(SurveyStatus.ACTIVE, 10L, pageable);
        verify(surveyRepository, never()).findVisibleToManagerByStatus(any(), any(), any());
    }

    @Test
    void getById_AsEmployee_IsNotFound_WhenNoQuestionsAreVisibleToThem() {
        mockAuthentication(100L, Role.EMPLOYEE);

        UserResponse empProfile = new UserResponse(100L, "EMP100", "Employee Name", "emp@test.com", Role.EMPLOYEE, 10L, 20L, "ACTIVE", 10L, "Manager Name", 20L, "HR Name", Instant.now(), Instant.now());
        when(userClient.getByIdInternal(100L)).thenReturn(empProfile);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.ACTIVE);
        survey.setCreatorManagerId(null);
        SurveyQuestion otherTeamsQuestion = new SurveyQuestion();
        otherTeamsQuestion.setCreatorManagerId(11L);
        otherTeamsQuestion.setSequenceOrder(1);
        survey.getQuestions().add(otherTeamsQuestion);
        when(surveyRepository.findById(1L)).thenReturn(java.util.Optional.of(survey));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(1L));
    }

    @Test
    void list_AsDEIManager_ReturnsOnlyOwnCreatedSurveys() {
        mockAuthentication(10L, Role.DEI_MANAGER);
        Pageable pageable = PageRequest.of(0, 10);

        Page<InclusionSurvey> expectedPage = new PageImpl<>(List.of(new InclusionSurvey()));
        when(surveyRepository.findVisibleToManager(10L, pageable)).thenReturn(expectedPage);

        Page<SurveyResponse> actual = service.list(null, pageable);

        assertNotNull(actual);
        verify(surveyRepository).findVisibleToManager(10L, pageable);
    }

    @Test
    void create_AsAdmin_RejectsInlineQuestions() {
        mockAuthentication(1L, Role.ADMIN);

        CreateSurveyRequest request = new CreateSurveyRequest("Annual", SurveyType.ANNUAL,
                LocalDate.now(), LocalDate.now().plusDays(7), true, 5,
                List.of(new CreateQuestionRequest("Do you feel included?", QuestionType.LIKERT_SCALE,
                        SurveyDimension.BELONGING, true, null)));

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(surveyRepository, never()).save(any(InclusionSurvey.class));
    }

    @Test
    void create_AsAdmin_LeavesSurveyOrgWide() {
        mockAuthentication(1L, Role.ADMIN);
        when(surveyRepository.save(any(InclusionSurvey.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateSurveyRequest request = new CreateSurveyRequest("Annual", SurveyType.ANNUAL,
                LocalDate.now(), LocalDate.now().plusDays(7), true, 5, List.of());

        service.create(request);

        ArgumentCaptor<InclusionSurvey> saved = ArgumentCaptor.forClass(InclusionSurvey.class);
        verify(surveyRepository).save(saved.capture());
        assertNull(saved.getValue().getCreatorManagerId());
    }

    @Test
    void launch_ThrowsForbidden_WhenManagerActsOnAnotherManagersSurvey() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.DRAFT);
        survey.setCreatorManagerId(11L);
        when(surveyRepository.findById(1L)).thenReturn(java.util.Optional.of(survey));

        assertThrows(ForbiddenOperationException.class, () -> service.launch(1L));
    }

    @Test
    void launch_NotifiesOnlyTheContributingManagersOwnEmployees() {
        mockAuthentication(1L, Role.ADMIN);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.DRAFT);
        survey.setCreatorManagerId(null);
        SurveyQuestion question = new SurveyQuestion();
        question.setCreatorManagerId(10L);
        survey.getQuestions().add(question);

        when(surveyRepository.findById(1L)).thenReturn(java.util.Optional.of(survey));
        when(surveyRepository.save(survey)).thenReturn(survey);
        when(userClient.getEmployeesByManagerInternal(10L)).thenReturn(List.of(
                employee(100L, "EMP100", 10L),
                employee(101L, "EMP101", 10L)));

        service.launch(1L);

        assertEquals(SurveyStatus.ACTIVE, survey.getStatus());
        ArgumentCaptor<EmitNotificationRequest> sent = ArgumentCaptor.forClass(EmitNotificationRequest.class);
        verify(notificationClient, times(2)).emitInternal(sent.capture());
        assertEquals(List.of("EMP100", "EMP101"),
                sent.getAllValues().stream().map(EmitNotificationRequest::employeeId).toList());
        assertEquals("SURVEY", sent.getAllValues().get(0).category());
        verify(userClient, never()).getEmployeesByManagerInternal(11L);
    }

    @Test
    void launch_SucceedsEvenWhenNotificationFanOutFails() {
        mockAuthentication(1L, Role.ADMIN);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.DRAFT);
        survey.setCreatorManagerId(null);
        SurveyQuestion question = new SurveyQuestion();
        question.setCreatorManagerId(10L);
        survey.getQuestions().add(question);

        when(surveyRepository.findById(1L)).thenReturn(java.util.Optional.of(survey));
        when(surveyRepository.save(survey)).thenReturn(survey);
        when(userClient.getEmployeesByManagerInternal(10L))
                .thenThrow(new RuntimeException("monolith unreachable"));

        assertDoesNotThrow(() -> service.launch(1L));
        assertEquals(SurveyStatus.ACTIVE, survey.getStatus());
    }

    private static UserResponse employee(Long id, String employeeId, Long managerId) {
        return new UserResponse(id, employeeId, "Employee " + id, employeeId + "@test.com",
                Role.EMPLOYEE, 10L, 20L, "ACTIVE", managerId, "Manager", 20L, "HR",
                Instant.now(), Instant.now());
    }

    @Test
    void launch_ThrowsForbidden_WhenManagerActsOnOrgWideSurvey() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.DRAFT);
        survey.setCreatorManagerId(null);
        when(surveyRepository.findById(1L)).thenReturn(java.util.Optional.of(survey));

        assertThrows(ForbiddenOperationException.class, () -> service.launch(1L));
    }
}
