package com.deiconnect.survey.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ForbiddenOperationException;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.survey.client.UserClient;
import com.deiconnect.survey.client.UserResponse;
import com.deiconnect.survey.dto.QuestionResponse;
import com.deiconnect.survey.entity.InclusionSurvey;
import com.deiconnect.survey.entity.SurveyQuestion;
import com.deiconnect.survey.enums.SurveyStatus;
import com.deiconnect.survey.mapper.SurveyQuestionMapper;
import com.deiconnect.survey.repository.SurveyQuestionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyQuestionServiceTest {

    @Mock private SurveyQuestionRepository questionRepository;
    @Mock private SurveyQuestionMapper questionMapper;
    @Mock private SurveyService surveyService;
    @Mock private UserClient userClient;
    @Mock private AuditLogWriter auditLogWriter;

    private SurveyQuestionService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new SurveyQuestionServiceImpl(questionRepository, questionMapper, surveyService, userClient, auditLogWriter);
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
    void listForSurvey_AsDEIManager_ReturnsOnlyOwnCreatedQuestions() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        when(questionRepository.findVisibleQuestions(1L, 10L))
                .thenReturn(List.of(new SurveyQuestion()));

        List<QuestionResponse> actual = service.listForSurvey(1L, null);

        assertNotNull(actual);
        verify(questionRepository).findVisibleQuestions(1L, 10L);
    }

    @Test
    void listForSurvey_AsEmployee_ReturnsOnlyAssignedManagerQuestions() {
        mockAuthentication(100L, Role.EMPLOYEE);

        UserResponse empProfile = new UserResponse(100L, "EMP100", "Employee Name", "emp@test.com", Role.EMPLOYEE, 10L, 20L, "ACTIVE", 10L, "Manager Name", 20L, "HR Name", Instant.now(), Instant.now());
        when(userClient.getByIdInternal(100L)).thenReturn(empProfile);

        when(questionRepository.findVisibleQuestions(1L, 10L))
                .thenReturn(List.of(new SurveyQuestion()));

        List<QuestionResponse> actual = service.listForSurvey(1L, null);

        assertNotNull(actual);
        verify(questionRepository).findVisibleQuestions(1L, 10L);
    }

    @Test
    void update_ThrowsForbidden_WhenManagerUpdatesQuestionOfAnotherManager() {
        mockAuthentication(10L, Role.DEI_MANAGER);

        InclusionSurvey survey = new InclusionSurvey();
        survey.setId(1L);
        survey.setStatus(SurveyStatus.DRAFT);

        SurveyQuestion question = new SurveyQuestion();
        question.setId(50L);
        question.setSurvey(survey);
        question.setCreatorManagerId(11L);

        when(questionRepository.findById(50L)).thenReturn(Optional.of(question));

        assertThrows(ForbiddenOperationException.class, () -> service.update(1L, 50L, null));
    }
}
