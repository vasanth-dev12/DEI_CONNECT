package com.deiconnect.diversity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.enums.Role;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.diversity.dto.DemographicProfileRequest;
import com.deiconnect.diversity.dto.DemographicProfileResponse;
import com.deiconnect.diversity.entity.DemographicProfile;
import com.deiconnect.diversity.enums.AgeGroup;
import com.deiconnect.diversity.enums.ConsentStatus;
import com.deiconnect.diversity.enums.DisabilityStatus;
import com.deiconnect.diversity.enums.Ethnicity;
import com.deiconnect.diversity.enums.Gender;
import com.deiconnect.diversity.enums.VeteranStatus;
import com.deiconnect.diversity.mapper.DemographicProfileMapper;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.iam.entity.User;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
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
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DemographicProfileServiceTest {

    @Mock
    private DemographicProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DemographicProfileMapper mapper;
    @Mock
    private AuditLogWriter auditLogWriter;

    private DemographicProfileService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new DemographicProfileServiceImpl(profileRepository, userRepository, mapper, auditLogWriter);
        originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(Long id) {
        DeiUserPrincipal principal = new DeiUserPrincipal(id, "EMP100", "emp@test.com", "pass", Role.EMPLOYEE, true);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void createOwn_Success() {
        mockAuthentication(10L);

        DemographicProfileRequest request = new DemographicProfileRequest(Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED);
        when(profileRepository.existsByEmployee_Id(10L)).thenReturn(false);

        User mockUser = new User();
        mockUser.setId(10L);
        when(userRepository.getReferenceById(10L)).thenReturn(mockUser);

        DemographicProfile savedProfile = new DemographicProfile();
        savedProfile.setId(1L);
        when(profileRepository.save(any(DemographicProfile.class))).thenReturn(savedProfile);

        DemographicProfileResponse res = new DemographicProfileResponse(1L, "EMP100", Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED, Instant.now(), Instant.now());
        when(mapper.toResponse(savedProfile)).thenReturn(res);

        DemographicProfileResponse actual = service.createOwn(request);

        assertNotNull(actual);
        assertEquals(1L, actual.profileId());
        verify(auditLogWriter).record("CREATE_PROFILE", "DemographicProfile", 1L);
    }

    @Test
    void createOwn_Conflict_AlreadyExists() {
        mockAuthentication(10L);
        DemographicProfileRequest request = new DemographicProfileRequest(Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED);

        when(profileRepository.existsByEmployee_Id(10L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.createOwn(request));
    }

    @Test
    void getOwn_Success() {
        mockAuthentication(10L);
        DemographicProfile profile = new DemographicProfile();
        profile.setId(1L);

        when(profileRepository.findByEmployee_Id(10L)).thenReturn(Optional.of(profile));

        DemographicProfileResponse res = new DemographicProfileResponse(1L, "EMP100", Gender.FEMALE, Ethnicity.WHITE, DisabilityStatus.NO, VeteranStatus.YES, AgeGroup.AGE_25_34, LocalDate.now(), ConsentStatus.CONSENTED, Instant.now(), Instant.now());
        when(mapper.toResponse(profile)).thenReturn(res);

        DemographicProfileResponse actual = service.getOwn();

        assertNotNull(actual);
        assertEquals(1L, actual.profileId());
        verify(auditLogWriter).record("VIEW_PROFILE", "DemographicProfile", 1L);
    }

    @Test
    void getOwn_NotFound() {
        mockAuthentication(10L);
        when(profileRepository.findByEmployee_Id(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getOwn());
    }
}
