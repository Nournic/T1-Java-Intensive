package ru.t1.nour.starter.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.t1.nour.starter.kafka.LogEventProducer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Aspect
@Slf4j
public class LoggingAspect {
    private final LogEventProducer logEventProducer;

    private static final Logger FALLBACK_LOGGER = LoggerFactory.getLogger("kafka-fallback");

    @Pointcut("@annotation(ru.t1.nour.starter.aop.annotation.LogDatasourceError)")
    public void logDataSourceError(){
    }

    @AfterThrowing(pointcut = "logDataSourceError()", throwing = "ex")
    public void logAfterException(JoinPoint jp, RuntimeException ex){
        CompletableFuture<SendResult<String, Object>> function = logEventProducer.sendLogEvent(
                "ERROR",
                jp.getSignature().toShortString(),
                jp.getArgs(),
                ex
        );

        function.whenComplete((result, exception)->{
            if(exception != null){
                log.error("Failed to send log event to Kafka asynchronously. Reason: {}\n" +
                        "Initiating fallback.", exception.getMessage());

                String fallbackMessage = String.format(
                        "Signature: %s | Args: %s",
                        jp.getSignature(),
                        Arrays.toString(jp.getArgs())
                );

                FALLBACK_LOGGER.error(fallbackMessage, ex);
            }
        });

        log.error(
                "Exception in {}. Args: {}",
                jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs()),
                ex
        );
    }
}
