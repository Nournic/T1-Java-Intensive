package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.model.Account;
import ru.t1.nour.microservice.model.Card;
import ru.t1.nour.microservice.model.Transaction;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;
import ru.t1.nour.microservice.model.enums.CardStatus;
import ru.t1.nour.microservice.model.enums.PaymentSystem;
import ru.t1.nour.microservice.model.enums.TransactionStatus;
import ru.t1.nour.microservice.model.enums.TransactionType;
import ru.t1.nour.microservice.repository.AccountRepository;
import ru.t1.nour.microservice.repository.CardRepository;
import ru.t1.nour.microservice.repository.TransactionRepository;
import ru.t1.nour.microservice.service.impl.kafka.KafkaEventProducer;
import ru.t1.nour.microservice.web.CreditApiFacade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private KafkaEventProducer kafkaEventProducer;
    @Mock
    private CreditApiFacade creditApiFacade;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private final long ANTIFRAUD_LIMIT_COUNT = 5L;
    private final long ANTIFRAUD_LIMIT_MINUTES = 1L;

    private Account account;
    private Card card;
    private TransactionEventDTO depositEvent;
    private UUID transactionKey;

    @BeforeEach
    void setUp() {
        // "Внедряем" значения в final-поля
        ReflectionTestUtils.setField(transactionService, "ANTIFRAUD_LIMIT_COUNT", ANTIFRAUD_LIMIT_COUNT);
        ReflectionTestUtils.setField(transactionService, "ANTIFRAUD_LIMIT_MINUTES", ANTIFRAUD_LIMIT_MINUTES);

        // Готовим общие тестовые данные
        account = new Account();
        ReflectionTestUtils.setField(account, "id", 1L);
        account.setBalance(new BigDecimal("1000"));
        account.setIsRecalc(false); // По умолчанию не кредитный

        card = new Card();
        ReflectionTestUtils.setField(card, "id", 10L);
        card.setStatus(CardStatus.ACTIVE);
        card.setCardId(1L);
        card.setAccount(account);
        card.setPaymentSystem(PaymentSystem.VISA);

        transactionKey = UUID.randomUUID();

        depositEvent = new TransactionEventDTO();
        depositEvent.setAccountId(account.getId());
        depositEvent.setCardId(card.getCardId());
        depositEvent.setAmount(new BigDecimal("200"));
        depositEvent.setType(TransactionType.DEPOSIT);
    }

    @Test
    void should_completeDeposit_when_cardIsActiveAndLimitsOk() {
        // --- ARRANGE ---
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        // Имитируем, что save() возвращает свой аргумент
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        // Антифрод-проверка проходит (транзакций меньше лимита)
        when(transactionRepository.countByCardIdAndTimestampBetween(eq(card.getCardId()), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(3L);

        // --- ACT ---
        transactionService.performTransaction(depositEvent, transactionKey);

        // --- ASSERT ---
        // 1. Проверяем, что баланс счета увеличился
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1200"));
        verify(accountRepository).save(account);

        // 2. Проверяем, что статус транзакции в итоге стал COMPLETE
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        // save вызывается 2 раза: PROCESSING -> COMPLETE
        verify(transactionRepository, times(2)).save(captor.capture());
        Transaction finalTransaction = captor.getValue();
        assertThat(finalTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETE);
    }

    @Test
    void should_cancelTransaction_when_cardIsBlocked() {
        // --- ARRANGE ---
        card.setStatus(CardStatus.BLOCKED); // Карта заблокирована
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        transactionService.performTransaction(depositEvent, transactionKey);

        // --- ASSERT ---
        // 1. Убеждаемся, что баланс НЕ изменился
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("1000"));

        // 2. Проверяем, что финальный статус транзакции - CANCELLED
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.CANCELLED);

        // 3. Проверяем, что до изменения баланса дело не дошло
        verify(accountRepository, never()).save(account);
    }

    @Test
    void should_blockCardAndTransaction_when_antifraudLimitExceeded() {
        // --- ARRANGE ---
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(cardRepository.findById(card.getCardId())).thenReturn(Optional.of(card));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        // Антифрод-проверка НЕ проходит (транзакций больше или равно лимиту)
        when(transactionRepository.countByCardIdAndTimestampBetween(eq(card.getCardId()), any(), any()))
                .thenReturn(ANTIFRAUD_LIMIT_COUNT + 1);

        // --- ACT ---
        transactionService.performTransaction(depositEvent, transactionKey);

        // --- ASSERT ---
        // 1. Карта должна быть заблокирована
        assertThat(card.getStatus()).isEqualTo(CardStatus.BLOCKED);

        // 2. Финальный статус транзакции - BLOCKED
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TransactionStatus.BLOCKED);

        // 3. Баланс не должен был измениться
        verify(accountRepository, never()).save(account);
    }

    @Test
    void should_triggerScheduledPayment_when_accountIsCreditAndDeposit() {
        // --- ARRANGE ---
        account.setIsRecalc(true); // Это кредитный счет
        account.setClientId(123L);
        account.setBalance(new BigDecimal("10000")); // Достаточно средств

        var nextPayment = new NextCreditPaymentDTO();
        nextPayment.setAmount(new BigDecimal("5000"));
        nextPayment.setPaymentDate(LocalDateTime.now().minusDays(1)); // День платежа уже наступил

        var cardForAccount = new Card();
        cardForAccount.setCardId(55L);

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardRepository.findById(card.getCardId())).thenReturn(Optional.of(card));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.countByCardIdAndTimestampBetween(anyLong(), any(), any())).thenReturn(1L);
        // Настраиваем фасад
        when(creditApiFacade.getNextPayment(account.getClientId())).thenReturn(Optional.of(nextPayment));
        when(cardRepository.findByAccount_Id(account.getId())).thenReturn(Optional.of(cardForAccount));

        // --- ACT ---
        transactionService.performTransaction(depositEvent, transactionKey);

        // --- ASSERT ---
        // Главная проверка: убедиться, что было отправлено событие на списание
        ArgumentCaptor<TransactionEventDTO> captor = ArgumentCaptor.forClass(TransactionEventDTO.class);
        verify(kafkaEventProducer).sendTransaction(captor.capture());

        TransactionEventDTO withdrawEvent = captor.getValue();
        assertThat(withdrawEvent.getType()).isEqualTo(TransactionType.WITHDRAW);
        assertThat(withdrawEvent.getAmount()).isEqualTo(new BigDecimal("5000"));
    }

    @Test
    void should_throwException_when_accountNotFound() {
        // ARRANGE
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> transactionService.performTransaction(depositEvent, transactionKey))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("is not found");

        verify(transactionRepository, never()).save(any());
    }
}
