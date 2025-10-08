package ru.t1.nour.starter.kafka;

import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public interface LogEventProducer {
    CompletableFuture<SendResult<String, Object>> sendLogEvent(String level, String signature, Object[] args, Throwable ex);

    void sendOutcomeHttpLog(String signature, String uri, Object[] args, Object responseBody);

    void sendIncomeHttpLog(String signature, String uri, String httpMethod, Object[] args);
}
