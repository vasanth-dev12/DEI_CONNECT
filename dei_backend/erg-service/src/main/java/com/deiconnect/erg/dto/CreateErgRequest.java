package com.deiconnect.erg.dto;

import com.deiconnect.erg.enums.ErgFocus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateErgRequest(

        @NotBlank @Size(max = 150) String ergName,

        @NotNull ErgFocus focus,

        @Size(max = 1000) String mission,

        @NotNull Long ergLeadId,

        Long executiveSponsorId,

        @PastOrPresent LocalDate foundedDate
) {
}
