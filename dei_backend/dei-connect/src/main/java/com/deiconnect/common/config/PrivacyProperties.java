package com.deiconnect.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.privacy")
@Getter
@Setter
public class PrivacyProperties {

    private int defaultMinGroupSize = 5;
}
