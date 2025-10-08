package ru.t1.nour.starter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.t1.nour.starter.aop.HttpLoggingAspect;
import ru.t1.nour.starter.aop.LoggingAspect;
import ru.t1.nour.starter.kafka.LogEventProducer;

@Configuration
@ConditionalOnBean(LogEventProducer.class)
public class T1StarterLogAspectConfig {
    public static final String LOGGING_ASPECT_BEAN_NAME = "t1LoggingAspect";

    public static final String HTTP_LOGGING_ASPECT_BEAN_NAME = "t1HttpLoggingAspect";


    @Bean(LOGGING_ASPECT_BEAN_NAME)
    public LoggingAspect logAspectBean(LogEventProducer logEventProducer){
        return new LoggingAspect(logEventProducer);
    }

    @Bean(HTTP_LOGGING_ASPECT_BEAN_NAME)
    public HttpLoggingAspect httpLogAspectBean(LogEventProducer logEventProducer){
        return new HttpLoggingAspect(logEventProducer);
    }
}
