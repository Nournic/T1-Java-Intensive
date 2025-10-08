package ru.t1.nour.starter.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.nour.starter.kafka.dto.LogErrorEventDTO;
import ru.t1.nour.starter.kafka.dto.LogIncomeHttpEventDTO;
import ru.t1.nour.starter.kafka.dto.LogOutcomeHttpEventDTO;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String TOPIC_LOG;

    private final String APP_NAME;

    private final ObjectMapper objectMapper;

    public void sendLogEvent(String level, String signature, Object[] args, Throwable ex){
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

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    TOPIC_LOG,
                    APP_NAME,
                    event
            );

            record.headers().add("type", level.getBytes());
            kafkaTemplate.send(record).whenComplete((result, exception)->{
                if(exception != null){
                    log.error("Failed to send log event to Kafka asynchronously. Reason: {}\n" +
                            "Initiating fallback.", exception.getMessage());

                    String fallbackMessage = String.format(
                            "Signature: %s | Args: %s",
                            signature,
                            Arrays.toString(args)
                    );

                    log.error(fallbackMessage, ex);
                }
            });
        } catch (Exception e) {
            log.error("Failed to send log event to Kafka", e);
        }
    }

    public void sendOutcomeHttpLog(String signature, String uri, Object[] args, Object responseBody){
        try{
            List<String> stringArgs = Arrays.stream(args)
                    .map(arg -> arg != null ? arg.toString() : null)
                    .toList();

            String bodyAsString = convertObjectToJson(responseBody);

            LogOutcomeHttpEventDTO event = new LogOutcomeHttpEventDTO(
                    Instant.now(),
                    signature,
                    uri,
                    stringArgs,
                    bodyAsString
            );

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    TOPIC_LOG,
                    APP_NAME,
                    event
            );

            record.headers().add("type","INFO".getBytes());

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if(ex != null)
                    log.error("Failed to send HTTP log to Kafka.", ex);
            });
        } catch (Exception e) {
            log.error("Failed to send log event to Kafka", e);
        }
    }

    public void sendIncomeHttpLog(String signature, String uri, String httpMethod, Object[] args) {
        try {
            List<String> stringArgs = Arrays.stream(args)
                    .map(arg -> arg != null ? arg.toString() : "null")
                    .collect(Collectors.toList());

            Object requestBody = findRequestBody(args);
            String bodyAsString = convertObjectToJson(requestBody);

            LogIncomeHttpEventDTO event = new LogIncomeHttpEventDTO(
                    Instant.now(),
                    signature,
                    uri,
                    httpMethod,
                    stringArgs,
                    bodyAsString
            );

            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    TOPIC_LOG,
                    APP_NAME,
                    event
            );

            record.headers().add("type", "INFO".getBytes());

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if(ex != null)
                    log.error("Failed to send HTTP log to Kafka.", ex);
            });
        } catch (Exception e) {
            log.error("Error preparing HTTP income log event for Kafka", e);
        }
    }

    private Object findRequestBody(Object[] args) {
        if (args == null) return null;

        return args.length > 0 ? args[0] : null;
    }

    private String convertObjectToJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response body to JSON", e);
            return "Error serializing object: " + object.toString();
        }
    }
}
