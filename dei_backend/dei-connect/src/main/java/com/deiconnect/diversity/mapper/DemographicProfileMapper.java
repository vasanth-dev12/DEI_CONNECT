package com.deiconnect.diversity.mapper;

import com.deiconnect.diversity.dto.DemographicProfileResponse;
import com.deiconnect.diversity.entity.DemographicProfile;
import org.springframework.stereotype.Component;

@Component
public class DemographicProfileMapper {

    public DemographicProfileResponse toResponse(DemographicProfile profile) {
        return new DemographicProfileResponse(
                profile.getId(),
                profile.getEmployeeId(),
                profile.getGender(),
                profile.getEthnicity(),
                profile.getDisability(),
                profile.getVeteranStatus(),
                profile.getAgeGroup(),
                profile.getDataCollectedDate(),
                profile.getConsentStatus(),
                profile.getCreatedDate(),
                profile.getLastModifiedDate());
    }
}
