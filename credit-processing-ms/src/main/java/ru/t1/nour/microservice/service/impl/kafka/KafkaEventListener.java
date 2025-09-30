package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.service.ProductRegistryService;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaEventListener {
    private final ProductRegistryService productRegistryService;

    @KafkaListener(topics = {"client_credit_products"})
    public void handleClientProductCreditEvent(ClientProductEventDTO event){
        log.info("Received ClientProductEvent: {}", event);

        productRegistryService.createByEvent(event);
    }
}
