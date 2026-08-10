package com.deiconnect.survey.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "deiconnect-backend", contextId = "notificationClient",
        fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/api/notifications/internal/emit")
    void emitInternal(@RequestBody EmitNotificationRequest request);
}
