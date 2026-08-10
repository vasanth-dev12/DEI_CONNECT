package com.deiconnect.goal.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.goal.dto.CreateGoalRequest;
import com.deiconnect.goal.dto.GoalResponse;
import com.deiconnect.goal.dto.UpdateGoalRequest;
import com.deiconnect.goal.entity.DEIGoal;
import com.deiconnect.goal.enums.GoalDimension;
import com.deiconnect.goal.enums.GoalStatus;
import com.deiconnect.goal.mapper.GoalMapper;
import com.deiconnect.goal.repository.DEIGoalRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DEIGoalServiceTest {

    private static final Long MANAGER_ID = 10L;

    @Mock
    private DEIGoalRepository goalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GoalMapper goalMapper;
    @Mock
    private AuditLogWriter auditLogWriter;

    private DEIGoalService service;

    @BeforeEach
    void setUp() {
        service = new DEIGoalServiceImpl(goalRepository, userRepository, goalMapper, auditLogWriter);
        SecurityContextHolder.clearContext();
        DeiUserPrincipal principal =
                DeiUserPrincipal.fromToken(MANAGER_ID, "MGR010", "mgr@x.io", Role.DEI_MANAGER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static CreateGoalRequest createRequest() {
        return new CreateGoalRequest("Increase diversity", GoalDimension.GENDER, "FEMALE",
                15.0, 30.0, 2027);
    }

    @Test
    void create_AssignsTheLoggedInUserAsOwner() {
        when(userRepository.existsById(MANAGER_ID)).thenReturn(true);
        User me = new User();
        me.setId(MANAGER_ID);
        when(userRepository.getReferenceById(MANAGER_ID)).thenReturn(me);
        when(goalRepository.save(any(DEIGoal.class))).thenAnswer(i -> {
            DEIGoal g = i.getArgument(0);
            g.setId(1L);
            return g;
        });

        service.create(createRequest());

        ArgumentCaptor<DEIGoal> saved = ArgumentCaptor.forClass(DEIGoal.class);
        verify(goalRepository).save(saved.capture());
        assertEquals(MANAGER_ID, saved.getValue().getOwner().getId());
        assertEquals(MANAGER_ID, saved.getValue().getCreatorManagerId());
        assertEquals(GoalStatus.ACTIVE, saved.getValue().getStatus());
        verify(auditLogWriter).record("CREATE_GOAL", "DEIGoal", 1L);
    }

    @Test
    void create_ThrowsResourceNotFound_WhenTheAuthenticatedUserIsMissing() {
        when(userRepository.existsById(MANAGER_ID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.create(createRequest()));
    }

    @Test
    void update_DoesNotReassignOwnership() {
        User originalOwner = new User();
        originalOwner.setId(99L);
        DEIGoal goal = new DEIGoal();
        goal.setId(1L);
        goal.setOwner(originalOwner);
        goal.setCreatorManagerId(MANAGER_ID);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any(DEIGoal.class))).thenAnswer(i -> i.getArgument(0));

        service.update(1L, new UpdateGoalRequest("Renamed", GoalDimension.GENDER, "FEMALE",
                15.0, 30.0, 2028, GoalStatus.OFF_TRACK));

        assertEquals(99L, goal.getOwner().getId(), "ownership must survive an edit untouched");
        assertEquals("Renamed", goal.getGoalName());
        assertEquals(GoalStatus.OFF_TRACK, goal.getStatus());
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    void mapper_ExposesOwnerNameForTheReadOnlyField() {
        GoalResponse res = new GoalResponse(1L, "G", GoalDimension.GENDER, "FEMALE", 15.0, 30.0,
                2027, MANAGER_ID, "Manager Ten", GoalStatus.ACTIVE, Instant.now(), Instant.now());
        assertEquals("Manager Ten", res.ownerName());
    }
}
