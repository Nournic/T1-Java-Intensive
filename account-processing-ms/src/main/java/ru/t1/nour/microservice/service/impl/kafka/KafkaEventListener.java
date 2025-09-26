package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.Transaction;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.service.AccountService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {
    private final AccountService accountService;

    @KafkaListener(topics = {"client_products"})
    public void handleClientProductEvent(ClientProductEventDTO event){
        log.info("Received ClientProductEvent: {}", event);

        accountService.registerNewAccount(event);
    }

    @KafkaListener(topics = {"client_transactions"})
    public void handleTransactionEvent(Transaction event){
        log.info("Received TransactionEvent: {}", event);

        //TODO: wait new functionality
    }

    @KafkaListener(topics = {"client_cards"})
    public void handleCardEvent(CardEventDTO event){
        log.info("Received CardEvent: {}", event);

        accountService.registerNewCard(event);
    }
}
