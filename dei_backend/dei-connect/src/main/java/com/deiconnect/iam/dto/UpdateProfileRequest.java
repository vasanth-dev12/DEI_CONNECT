package com.deiconnect.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank @Size(max = 150)
        String name,

        @NotBlank @Email @Size(max = 190)
        String email,

        @Size(min = 8, max = 100)
        String password
) {
}
