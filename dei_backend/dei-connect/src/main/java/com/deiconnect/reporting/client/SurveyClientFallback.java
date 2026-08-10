package com.deiconnect.reporting.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SurveyClientFallback implements SurveyClient {
    private static final Logger log = LoggerFactory.getLogger(SurveyClientFallback.class);

    @Override
    public Double getAverageInclusionIndex(String scope, String scopeValue) {
        log.error("Survey microservice is down/unreachable. Fallback invoked for average inclusion index lookup. Scope: {}, value: {}", scope, scopeValue);
        return 0.0;
    }
}
