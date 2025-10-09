package ru.t1.nour.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "t1.app.starter.logging")
public class LoggingAspectProperties {
    private boolean enabled = true;

    private String topic = "service_logs";
}
