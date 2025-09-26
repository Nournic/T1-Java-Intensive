package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardEventProducer {
    private static final String TOPIC_CLIENT_CARDS = "client_cards";

    private final KafkaTemplate<String, CardEventDTO> kafkaTemplate;

    public void sendCardEvent(CardEventDTO event){
        kafkaTemplate.send(TOPIC_CLIENT_CARDS, event);
    }
}
