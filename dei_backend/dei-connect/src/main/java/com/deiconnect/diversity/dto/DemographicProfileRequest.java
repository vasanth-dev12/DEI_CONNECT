package com.deiconnect.diversity.dto;

import com.deiconnect.diversity.enums.AgeGroup;
import com.deiconnect.diversity.enums.ConsentStatus;
import com.deiconnect.diversity.enums.DisabilityStatus;
import com.deiconnect.diversity.enums.Ethnicity;
import com.deiconnect.diversity.enums.Gender;
import com.deiconnect.diversity.enums.VeteranStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record DemographicProfileRequest(

        @NotNull(message = "Gender is required — select 'Prefer not to say' if you would rather not disclose it")
        Gender gender,

        @NotNull(message = "Ethnicity is required — select 'Prefer not to say' if you would rather not disclose it")
        Ethnicity ethnicity,

        @NotNull(message = "Disability status is required — select 'Prefer not to say' if you would rather not disclose it")
        DisabilityStatus disability,

        @NotNull(message = "Veteran status is required — select 'Prefer not to say' if you would rather not disclose it")
        VeteranStatus veteranStatus,

        @NotNull(message = "Age group is required — select 'Prefer not to say' if you would rather not disclose it")
        AgeGroup ageGroup,

        @PastOrPresent LocalDate dataCollectedDate,

        @NotNull(message = "Consent status is required")
        ConsentStatus consentStatus
) {
}
