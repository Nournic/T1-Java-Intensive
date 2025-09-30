package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;

@Component
@RequiredArgsConstructor
public class KafkaEventProducer {
    private static final String TOPIC_CLIENT_TRANSACTION = "client_transaction";

    private final KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    public void sendTransaction(TransactionEventDTO event){
        String key = event.getTransactionId().toString();
        kafkaTemplate.send(TOPIC_CLIENT_TRANSACTION, key, event);
    }
}
