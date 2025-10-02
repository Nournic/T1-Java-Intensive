package ru.t1.nour.microservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.t1.nour.microservice.service.impl.kafka.LogEventProducer;

@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class HttpLoggingAspect {
    private final LogEventProducer logEventProducer;

    private static final Logger FALLBACK_LOGGER = LoggerFactory.getLogger("kafka-fallback");

    @AfterReturning(pointcut = "@annotation(ru.t1.nour.microservice.aop.annotation.HttpOutcomeRequestLog)",
            returning = "result")
    public void logAfterHttpRequest(JoinPoint jp, Object result){
        String uri = "URI not available";
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            uri = request.getRequestURI();
        } catch (IllegalStateException e) {
            log.warn("Could not retrieve HTTP request details for logging. Aspect may be invoked outside of a request context.");
        }

        logEventProducer.sendOutcomeHttpLog(
                jp.getSignature().toShortString(),
                uri,
                jp.getArgs(),
                result
        );
    }

    @Before("@annotation(ru.t1.nour.microservice.aop.annotation.HttpIncomeRequestLog)")
    public void logBeforeHttpRequest(JoinPoint jp){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        logEventProducer.sendIncomeHttpLog(
                jp.getSignature().toShortString(),
                request.getRequestURI(),
                request.getMethod(),
                jp.getArgs()
        );
    }
}
