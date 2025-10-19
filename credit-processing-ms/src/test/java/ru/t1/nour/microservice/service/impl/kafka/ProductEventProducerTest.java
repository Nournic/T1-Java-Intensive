package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, ClientProductEventDTO> kafkaTemplate;

    @InjectMocks
    private ProductEventProducer productEventProducer;

    private static final String TOPIC_CLIENT_PRODUCTS = "client_products";

    @Test
    void should_sendEventToCorrectTopic() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        event.setClientId(1L);

        // ACT
        productEventProducer.sendProductEvent(event);

        // ASSERT
        verify(kafkaTemplate, times(1)).send(TOPIC_CLIENT_PRODUCTS, event);
    }
}
