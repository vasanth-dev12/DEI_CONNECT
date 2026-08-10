package com.deiconnect.reporting.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ErgClientFallback implements ErgClient {
    private static final Logger log = LoggerFactory.getLogger(ErgClientFallback.class);

    @Override
    public long getActiveMemberCount(String scope, String scopeValue, Long hrId) {
        log.error("ERG microservice is down/unreachable. Fallback invoked for active member count lookup. Scope: {}, value: {}, hrId: {}", scope, scopeValue, hrId);
        return 0L;
    }
}
