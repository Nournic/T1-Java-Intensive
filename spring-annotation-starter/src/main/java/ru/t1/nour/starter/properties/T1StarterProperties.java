package ru.t1.nour.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "t1.app.starter")
public class T1StarterProperties {
    private String appName;

    @NestedConfigurationProperty
    private LoggingAspectProperties logging = new LoggingAspectProperties();

    @NestedConfigurationProperty
    private MetricAspectProperties metrics = new MetricAspectProperties();

    @NestedConfigurationProperty
    private CachingAspectProperties caching = new CachingAspectProperties();
}
