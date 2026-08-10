package com.deiconnect.survey.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationClientFallback implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientFallback.class);

    @Override
    public void emitInternal(EmitNotificationRequest request) {
        log.error("Notification service unreachable — dropped {} notification for employeeId={}: {}",
                request.category(), request.employeeId(), request.message());
    }
}
