package ru.t1.nour.microservice.service.impl.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ExternalPaymentEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;
import ru.t1.nour.microservice.model.enums.TransactionStatus;
import ru.t1.nour.microservice.model.enums.TransactionType;
import ru.t1.nour.microservice.service.AccountService;
import ru.t1.nour.microservice.service.TransactionService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {
    private final AccountService accountService;

    private final KafkaEventProducer kafkaEventProducer;

    private final TransactionService transactionService;

    @KafkaListener(topics = {"client_products"})
    public void handleClientProductEvent(ClientProductEventDTO event){
        log.info("Received ClientProductEvent: {}", event);

        accountService.registerNewAccount(event);
    }

    @KafkaListener(topics = {"client_transactions"})
    public void handleTransactionEvent(TransactionEventDTO event,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String key){
        log.info("Received TransactionEvent: {}", event);

        transactionService.performTransaction(event, UUID.fromString(key));
    }

    @KafkaListener(topics = {"client_cards"})
    public void handleCardEvent(CardEventDTO event){
        log.info("Received CardEvent: {}", event);

        accountService.registerNewCard(event);
    }

    @KafkaListener(topics = "client_payments")
    public void handleExternalPayment(ExternalPaymentEventDTO externalEvent,
                                      @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Received external payment event: {}", externalEvent);

        try {
            TransactionEventDTO internalCommand = new TransactionEventDTO(
                    UUID.fromString(key), // UUID
                    externalEvent.getAccountId(),
                    externalEvent.getCardId(),
                    TransactionType.WITHDRAW,
                    externalEvent.getAmount(),
                    TransactionStatus.ALLOWED,
                    LocalDateTime.now(),
                    "PROCESS_TRANSACTION"
            );

            kafkaEventProducer.sendTransaction(internalCommand);

            log.info("Translated external payment {} to internal transaction command.", externalEvent.getPaymentID().toString());

        } catch (Exception e) {
            log.error("Failed to translate external payment event: {}. Reason: {}",
                    externalEvent, e.getMessage());
        }
    }
}
