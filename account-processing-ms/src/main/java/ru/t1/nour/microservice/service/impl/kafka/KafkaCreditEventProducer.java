package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;

@Component
@RequiredArgsConstructor
public class KafkaCreditEventProducer {
    private static final String TOPIC_CLIENT_TRANSACTION = "credit_payment";

    private final KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    public void sendTransaction(TransactionEventDTO event){
        kafkaTemplate.send(TOPIC_CLIENT_TRANSACTION, event);
    }
}
