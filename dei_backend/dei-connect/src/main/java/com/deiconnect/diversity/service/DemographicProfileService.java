package com.deiconnect.diversity.service;

import com.deiconnect.diversity.dto.DemographicProfileRequest;
import com.deiconnect.diversity.dto.DemographicProfileResponse;

public interface DemographicProfileService {

    DemographicProfileResponse createOwn(DemographicProfileRequest payload);

    DemographicProfileResponse getOwn();

    DemographicProfileResponse updateOwn(DemographicProfileRequest payload);
}