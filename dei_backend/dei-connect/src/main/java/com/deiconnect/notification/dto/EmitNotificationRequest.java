package com.deiconnect.notification.dto;

import com.deiconnect.notification.enums.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmitNotificationRequest(

        @NotBlank @Size(max = 64) String employeeId,

        @NotNull NotificationCategory category,

        @NotBlank @Size(max = 500) String message
) {
}
