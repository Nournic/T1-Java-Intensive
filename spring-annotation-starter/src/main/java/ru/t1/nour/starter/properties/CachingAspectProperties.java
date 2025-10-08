package ru.t1.nour.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "t1.app.starter.caching")
public class CachingAspectProperties {
    private boolean enabled = true;

    private long ttlMinutes = 5;

    private long maximumSize = 500;
}
