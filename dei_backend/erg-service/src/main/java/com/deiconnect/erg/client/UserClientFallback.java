package com.deiconnect.erg.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {
    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public UserResponse getByIdInternal(Long id) {
        log.error("Monolith user service is down/unreachable. Fallback invoked for user ID: {}", id);
        return new UserResponse(
                id,
                "OFFLINE",
                "System User (Cached/Offline)",
                "system-offline@deiconnect.local",
                com.deiconnect.common.enums.Role.EMPLOYEE,
                null,
                null,
                "INACTIVE",
                null,
                null,
                null,
                null,
                java.time.Instant.now(),
                java.time.Instant.now()
        );
    }

    @Override
    public java.util.List<UserResponse> getByIdsInternal(java.util.List<Long> ids) {
        log.error("Monolith user service is down/unreachable. Fallback invoked for batch IDs: {}", ids);
        return java.util.List.of();
    }
}
