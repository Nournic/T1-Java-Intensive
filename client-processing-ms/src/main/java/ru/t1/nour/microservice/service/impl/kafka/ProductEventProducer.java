package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {
    private static final String TOPIC_CLIENT_PRODUCTS = "client_products";

    private static final String TOPIC_CLIENT_CREDIT_PRODUCTS = "client_credit_products";


    private final KafkaTemplate<String, ClientProductEventDTO> kafkaTemplate;

    public void sendProductEvent(ClientProductEventDTO event){
        String productKey = event.getProductKey();

        if(isStandardProduct(productKey))
            kafkaTemplate.send(TOPIC_CLIENT_PRODUCTS, event);
        else if(isCreditProduct(productKey))
            kafkaTemplate.send(TOPIC_CLIENT_CREDIT_PRODUCTS, event);
        else log.error("Unknown product type for event bus: {}", productKey);

    }

    private boolean isStandardProduct(String key){
        return "DC".equals(key) || "CC".equals(key) || "NS".equals(key) || "PENS".equals(key);
    }

    private boolean isCreditProduct(String key){
        return "IPO".equals(key) || "PC".equals(key) || "AC".equals(key);
    }
}
