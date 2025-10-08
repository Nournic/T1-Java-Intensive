package ru.t1.nour.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "t1.app.starter.metrics")
public class MetricAspectProperties {
    private boolean enabled = true;

    private long executionTimeLimitMs = 2000;
}
