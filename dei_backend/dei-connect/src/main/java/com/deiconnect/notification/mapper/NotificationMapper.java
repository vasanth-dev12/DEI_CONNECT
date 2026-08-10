package com.deiconnect.notification.mapper;

import com.deiconnect.notification.dto.NotificationResponse;
import com.deiconnect.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEmployeeId(),
                notification.getMessage(),
                notification.getCategory(),
                notification.getStatus(),
                notification.getCreatedDate());
    }
}
