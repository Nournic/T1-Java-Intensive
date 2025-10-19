package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CardEventProducerTest {

    @Mock
    private KafkaTemplate<String, CardEventDTO> kafkaTemplate;

    private static final String TOPIC_CLIENT_CARDS = "client_cards";

    @InjectMocks
    private CardEventProducer cardEventProducer;

    @Test
    void should_sendEventToCorrectTopic_when_sendCardEventIsCalled() {
        // --- ARRANGE ---
        // Создаем любое тестовое событие
        CardEventDTO testEvent = new CardEventDTO();
        testEvent.setAccountId(123L);

        // --- ACT ---
        // Вызываем тестируемый метод
        cardEventProducer.sendCardEvent(testEvent);

        // --- ASSERT ---

        verify(kafkaTemplate, times(1)).send(TOPIC_CLIENT_CARDS, testEvent);
    }

}
