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
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {
    private final LogEventProducer logEventProducer;

    private final long LIMIT_TIME_FOR_EXECUTION;

    @Pointcut("@annotation(ru.t1.nour.starter.aop.annotation.Metric)")
    public void performMetricMethod(){
    }

    @Around("performMetricMethod()")
    public Object logExecutionTime(ProceedingJoinPoint jp) throws Throwable{
        long start = System.currentTimeMillis();
        Object object = jp.proceed();
        long time = System.currentTimeMillis() - start;

        if(time > LIMIT_TIME_FOR_EXECUTION) {
            logEventProducer.sendLogEvent(
                    "WARNING",
                    jp.getSignature().toShortString(),
                    jp.getArgs(),
                    null
            );

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
