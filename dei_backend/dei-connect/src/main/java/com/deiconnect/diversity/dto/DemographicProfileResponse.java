package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.AgeGroup;
import com.deiconnect.diversity.enums.ConsentStatus;
import com.deiconnect.diversity.enums.DisabilityStatus;
import com.deiconnect.diversity.enums.Ethnicity;
import com.deiconnect.diversity.enums.Gender;
import com.deiconnect.diversity.enums.VeteranStatus;

import java.time.Instant;
import java.time.LocalDate;

public record DemographicProfileResponse(
        Long profileId,
        String employeeId,
        Gender gender,
        Ethnicity ethnicity,
        DisabilityStatus disability,
        VeteranStatus veteranStatus,
        AgeGroup ageGroup,
        LocalDate dataCollectedDate,
        ConsentStatus consentStatus,
        Instant createdDate,
        Instant lastModifiedDate
) {
}
