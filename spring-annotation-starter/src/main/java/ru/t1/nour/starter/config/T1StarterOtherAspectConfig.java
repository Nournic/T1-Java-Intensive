package ru.t1.nour.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.nour.starter.aop.CachedAspect;
import ru.t1.nour.starter.aop.MetricAspect;
import ru.t1.nour.starter.kafka.LogEventProducer;

@Configuration
public class T1StarterOtherAspectConfig {
    public static final String CACHE_ASPECT_BEAN_NAME = "t1CacheAspect";

    public static final String METRIC_ASPECT_BEAN_NAME = "t1MetricAspect";

    @Bean(METRIC_ASPECT_BEAN_NAME)
    @ConditionalOnBean(LogEventProducer.class)
    public MetricAspect metricAspectBean(LogEventProducer logEventProducer){
        return new MetricAspect(logEventProducer);
    }

    @Bean(CACHE_ASPECT_BEAN_NAME)
    @ConditionalOnBean(CacheManager.class)
    public CachedAspect cachedAspectBean(CacheManager cacheManager){
        return new CachedAspect(cacheManager);
    }
}
