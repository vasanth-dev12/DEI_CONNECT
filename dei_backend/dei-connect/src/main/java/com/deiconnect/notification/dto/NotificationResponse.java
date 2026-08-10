package com.deiconnect.notification.dto;

import com.deiconnect.notification.enums.NotificationCategory;
import com.deiconnect.notification.enums.NotificationStatus;

import java.time.Instant;

public record NotificationResponse(
        Long notificationId,
        String employeeId,
        String message,
        NotificationCategory category,
        NotificationStatus status,
        Instant createdDate
) {
}
