package com.deiconnect.diversity.service;

import com.deiconnect.common.audit.AuditLogWriter;
import com.deiconnect.common.exception.ConflictException;
import com.deiconnect.common.exception.ResourceNotFoundException;
import com.deiconnect.diversity.dto.DemographicProfileRequest;
import com.deiconnect.diversity.dto.DemographicProfileResponse;
import com.deiconnect.diversity.entity.DemographicProfile;
import com.deiconnect.diversity.mapper.DemographicProfileMapper;
import com.deiconnect.diversity.repository.DemographicProfileRepository;
import com.deiconnect.iam.repository.UserRepository;
import com.deiconnect.security.DeiUserPrincipal;
import com.deiconnect.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DemographicProfileServiceImpl implements DemographicProfileService {

    private final DemographicProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final DemographicProfileMapper profileMapper;
    private final AuditLogWriter auditLogWriter;

    @Override
    @Transactional
    public DemographicProfileResponse createOwn(DemographicProfileRequest payload) {
        DeiUserPrincipal userPrincipal = SecurityUtils.requireCurrentPrincipal();
        if (profileRepository.existsByEmployee_Id(userPrincipal.getId())) {
            throw new ConflictException("A demographic profile already exists for this employee");
        }

        DemographicProfile demographicProfile = DemographicProfile.builder()
                .employee(userRepository.getReferenceById(userPrincipal.getId()))
                .employeeId(userPrincipal.getEmployeeId())
                .gender(payload.gender())
                .ethnicity(payload.ethnicity())
                .disability(payload.disability())
                .veteranStatus(payload.veteranStatus())
                .ageGroup(payload.ageGroup())
                .dataCollectedDate(payload.dataCollectedDate() == null
                        ? LocalDate.now() : payload.dataCollectedDate())
                .consentStatus(payload.consentStatus())
                .build();

        demographicProfile = profileRepository.save(demographicProfile);
        auditLogWriter.record("CREATE_PROFILE", "DemographicProfile", demographicProfile.getId());
        return profileMapper.toResponse(demographicProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public DemographicProfileResponse getOwn() {
        DemographicProfile demographicProfile = loadOwn();
        auditLogWriter.record("VIEW_PROFILE", "DemographicProfile", demographicProfile.getId());
        return profileMapper.toResponse(demographicProfile);
    }

    @Override
    @Transactional
    public DemographicProfileResponse updateOwn(DemographicProfileRequest payload) {
        DemographicProfile demographicProfile = loadOwn();
        demographicProfile.setGender(payload.gender());
        demographicProfile.setEthnicity(payload.ethnicity());
        demographicProfile.setDisability(payload.disability());
        demographicProfile.setVeteranStatus(payload.veteranStatus());
        demographicProfile.setAgeGroup(payload.ageGroup());
        if (payload.dataCollectedDate() != null) {
            demographicProfile.setDataCollectedDate(payload.dataCollectedDate());
        }
        demographicProfile.setConsentStatus(payload.consentStatus());

        demographicProfile = profileRepository.save(demographicProfile);
        auditLogWriter.record("UPDATE_PROFILE", "DemographicProfile", demographicProfile.getId());
        return profileMapper.toResponse(demographicProfile);
    }

    private DemographicProfile loadOwn() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return profileRepository.findByEmployee_Id(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No demographic profile found for the current user"));
    }
}