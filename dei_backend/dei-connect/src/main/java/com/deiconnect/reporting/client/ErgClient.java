package com.deiconnect.reporting.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "erg-service", fallback = ErgClientFallback.class)
public interface ErgClient {

    @GetMapping("/api/ergs/internal/members/count")
    long getActiveMemberCount(@RequestParam("scope") String scope,
                              @RequestParam("scopeValue") String scopeValue,
                              @RequestParam("hrId") Long hrId);
}
