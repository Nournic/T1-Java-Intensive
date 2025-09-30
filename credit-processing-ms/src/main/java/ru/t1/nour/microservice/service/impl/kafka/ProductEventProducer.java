package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {
    private static final String TOPIC_CLIENT_PRODUCTS = "client_products";

    private final KafkaTemplate<String, ClientProductEventDTO> kafkaTemplate;

    public void sendProductEvent(ClientProductEventDTO event){
        kafkaTemplate.send(TOPIC_CLIENT_PRODUCTS, event);
    }
}
