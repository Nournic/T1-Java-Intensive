package ru.t1.nour.starter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.nour.starter.aop.CachedAspect;
import ru.t1.nour.starter.aop.HttpLoggingAspect;
import ru.t1.nour.starter.aop.LoggingAspect;
import ru.t1.nour.starter.aop.MetricAspect;
import ru.t1.nour.starter.kafka.LogEventProducer;
import ru.t1.nour.starter.properties.T1StarterProperties;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({
        T1StarterProperties.class
})
public class T1StarterAutoConfiguration {
    @Configuration
    @ConditionalOnProperty(name = "t1.app.starter.logging.enabled", havingValue = "true", matchIfMissing = true)
    static class LoggingAspectConfiguration {

        @Bean
        @ConditionalOnBean(KafkaTemplate.class)
        @ConditionalOnMissingBean
        public LogEventProducer t1LogEventProducer(
                KafkaTemplate<String, Object> kafkaTemplate,
                ObjectMapper objectMapper,
                T1StarterProperties properties,
                @Value("${spring.application.name:unknown-service}") String appNameFromSpring
        ) {
            String appName = properties.getAppName() != null ? properties.getAppName() : appNameFromSpring;

            return new LogEventProducer(
                    kafkaTemplate,
                    properties.getLogging().getTopic(),
                    appName,
                    objectMapper
            );
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(LogEventProducer.class)
        public LoggingAspect t1LoggingAspect(LogEventProducer producer) {
            return new LoggingAspect(producer);
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(LogEventProducer.class)
        public HttpLoggingAspect t1HttpIncomeLoggingAspect(LogEventProducer producer) {
            return new HttpLoggingAspect(producer);
        }
    }


    @Configuration
    @ConditionalOnProperty(name = "t1.app.starter.metrics.enabled", havingValue = "true", matchIfMissing = true)
    static class MetricAspectConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnBean(LogEventProducer.class)
        public MetricAspect t1MetricAspect(LogEventProducer producer, T1StarterProperties properties) {
            return new MetricAspect(producer, properties.getMetrics().getExecutionTimeLimitMs());
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "t1.app.starter.caching.enabled", havingValue = "true", matchIfMissing = true)
    static class CachingAspectConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public CacheManager caffeineCacheManager(T1StarterProperties properties) {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager();
            Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                    .expireAfterWrite(properties.getCaching().getTtlMinutes(), TimeUnit.MINUTES)
                    .maximumSize(properties.getCaching().getMaximumSize());
            cacheManager.setCaffeine(caffeine);
            return cacheManager;
        }

        @Bean
        @ConditionalOnMissingBean
        public CachedAspect t1CachingAspect(CacheManager cacheManager) {
            return new CachedAspect(cacheManager);
        }
    }
}
