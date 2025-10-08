package ru.t1.nour.starter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "t1.starter.config")
public class T1StarterProperties {
    private boolean enabled = true;

    private
}
