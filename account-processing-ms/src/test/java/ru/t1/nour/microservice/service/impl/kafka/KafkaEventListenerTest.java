package ru.t1.nour.microservice.service.impl.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ExternalPaymentEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;
import ru.t1.nour.microservice.model.enums.TransactionType;
import ru.t1.nour.microservice.service.AccountService;
import ru.t1.nour.microservice.service.TransactionService;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaEventListenerTest {
    @Mock
    private AccountService accountService;
    @Mock
    private KafkaEventProducer kafkaEventProducer;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private KafkaEventListener kafkaEventListener;

    @Test
    void should_callRegisterNewAccount_when_handleClientProductEvent() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        doNothing().when(accountService).registerNewAccount(event);

        // ACT
        kafkaEventListener.handleClientProductEvent(event);

        // ASSERT
        verify(accountService, times(1)).registerNewAccount(event);
    }

    @Test
    void should_callPerformTransaction_when_handleTransactionEvent() {
        // ARRANGE
        var event = new TransactionEventDTO();
        String key = UUID.randomUUID().toString();
        doNothing().when(transactionService).performTransaction(event, UUID.fromString(key));

        // ACT
        kafkaEventListener.handleTransactionEvent(event, key);

        // ASSERT
        verify(transactionService, times(1)).performTransaction(event, UUID.fromString(key));
    }

    @Test
    void should_callRegisterNewCard_when_handleCardEvent() {
        // ARRANGE
        var event = new CardEventDTO();
        doNothing().when(accountService).registerNewCard(event);

        // ACT
        kafkaEventListener.handleCardEvent(event);

        // ASSERT
        verify(accountService, times(1)).registerNewCard(event);
    }

    @Test
    void should_translateAndSendEvent_when_handleExternalPayment() {
        // ARRANGE
        var externalEvent = new ExternalPaymentEventDTO();
        externalEvent.setPaymentID(UUID.randomUUID());
        // ... (заполняем остальные поля)

        String key = externalEvent.getPaymentID().toString();

        // ACT
        kafkaEventListener.handleExternalPayment(externalEvent, key);

        // ASSERT
        // "Ловим" событие, которое было отправлено в другой топик
        ArgumentCaptor<TransactionEventDTO> captor = ArgumentCaptor.forClass(TransactionEventDTO.class);
        verify(kafkaEventProducer, times(1)).sendTransaction(captor.capture());

        // Проверяем содержимое "переведенного" события
        TransactionEventDTO internalCommand = captor.getValue();
        assertThat(internalCommand.getTransactionId().toString()).isEqualTo(key);
        assertThat(internalCommand.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(internalCommand.getEventType()).isEqualTo("PROCESS_TRANSACTION");
    }

    @Test
    void should_notSendEvent_when_translationFailsInExternalPayment() {
        // Тестируем блок try-catch
        // ARRANGE
        var externalEvent = new ExternalPaymentEventDTO();
        // Передаем невалидный UUID в ключе, чтобы спровоцировать ошибку
        String invalidKey = "not-a-uuid";

        // ACT
        // Мы не ожидаем исключения, т.к. оно ловится внутри метода
        kafkaEventListener.handleExternalPayment(externalEvent, invalidKey);

        // ASSERT
        // Главная проверка - убедиться, что до отправки события дело не дошло
        verify(kafkaEventProducer, never()).sendTransaction(any());
    }
}
