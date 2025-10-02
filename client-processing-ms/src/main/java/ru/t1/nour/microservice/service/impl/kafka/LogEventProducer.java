package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.nour.microservice.model.dto.kafka.LogErrorEventDTO;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogEventProducer {
    private static final String TOPIC_LOG = "service_logs";

    private final KafkaTemplate<String, LogErrorEventDTO> kafkaTemplate;

    @Value("${t1.app.name}")
    private String APP_NAME;

    @Transactional
    public CompletableFuture<SendResult<String, LogErrorEventDTO>> sendLogEvent(String level, String signature, Object[] args, Throwable ex){
        try{
            List<String> stringArgs = Arrays.stream(args)
                    .map(arg -> arg != null ? arg.toString() : null)
                    .toList();

            LogErrorEventDTO event = new LogErrorEventDTO(
                    Instant.now(),
                    signature,
                    stringArgs,
                    ex != null ? ex.getMessage() : null,
                    ExceptionUtils.getStackTrace(ex)
            );

            ProducerRecord<String, LogErrorEventDTO> record = new ProducerRecord<>(
                    TOPIC_LOG,
                    APP_NAME,
                    event
            );

            record.headers().add("type", level.getBytes());
            return kafkaTemplate.send(record);
        } catch (Exception e) {
            log.error("Failed to send log event to Kafka", e);

            return CompletableFuture.failedFuture(e);
        }
    }
}
