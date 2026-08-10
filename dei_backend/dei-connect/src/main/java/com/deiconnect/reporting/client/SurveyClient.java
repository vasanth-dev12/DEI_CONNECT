package com.deiconnect.reporting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "survey-service", fallback = SurveyClientFallback.class)
public interface SurveyClient {

    @GetMapping("/api/surveys/internal/inclusion-index/average")
    Double getAverageInclusionIndex(@RequestParam(value = "scope", required = false) String scope,
                                    @RequestParam(value = "scopeValue", required = false) String scopeValue);
}
