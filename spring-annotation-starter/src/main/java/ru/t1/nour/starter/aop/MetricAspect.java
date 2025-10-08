package ru.t1.nour.starter.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.t1.nour.starter.kafka.LogEventProducer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {
    private final LogEventProducer logEventProducer;

    private static final Logger FALLBACK_LOGGER = LoggerFactory.getLogger("kafka-fallback");

    @Value("${t1.app.aop.limit_time_for_execution}")
    private long LIMIT_TIME_FOR_EXECUTION;

    @Pointcut("@annotation(ru.t1.nour.starter.aop.annotation.Metric)")
    public void performMetricMethod(){
    }

    @Around("performMetricMethod()")
    public Object logExecutionTime(ProceedingJoinPoint jp) throws Throwable{
        long start = System.currentTimeMillis();
        Object object = jp.proceed();
        long time = System.currentTimeMillis() - start;

        if(time > LIMIT_TIME_FOR_EXECUTION) {
            CompletableFuture<SendResult<String, Object>> function = logEventProducer.sendLogEvent(
                    "WARNING",
                    jp.getSignature().toShortString(),
                    jp.getArgs(),
                    null
            );

            function.whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Failed to send log event to Kafka asynchronously. Reason: {}\n" +
                            "Initiating fallback.", exception.getMessage());

                    String fallbackMessage = String.format(
                            "Signature: %s | Args: %s",
                            jp.getSignature(),
                            Arrays.toString(jp.getArgs())
                    );

                    FALLBACK_LOGGER.error(fallbackMessage, exception);
                }
            });

            log.warn(
                    "Method {} took too long to execute. Execution time {}. Args: {}",
                    jp.getSignature().toShortString(),
                    time,
                    Arrays.toString(jp.getArgs())
            );
        }

        return object;
    }


}
