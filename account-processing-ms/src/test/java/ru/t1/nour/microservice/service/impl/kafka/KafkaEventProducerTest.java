package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaEventProducerTest {
    @Mock
    private KafkaTemplate<String, TransactionEventDTO> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducer kafkaEventProducer;

    private static final String TOPIC_CLIENT_TRANSACTION = "client_transaction";

    @Test
    void should_sendEventToCorrectTopic() {
        // ARRANGE
        var testEvent = new TransactionEventDTO();

        // ACT
        kafkaEventProducer.sendTransaction(testEvent);

        // ASSERT
        verify(kafkaTemplate, times(1)).send(TOPIC_CLIENT_TRANSACTION, testEvent);
    }
}
