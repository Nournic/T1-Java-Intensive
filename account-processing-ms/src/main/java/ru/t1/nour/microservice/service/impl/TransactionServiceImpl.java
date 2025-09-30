package ru.t1.nour.microservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.Account;
import ru.t1.nour.microservice.model.Card;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.model.dto.kafka.TransactionEventDTO;
import ru.t1.nour.microservice.model.enums.CardStatus;
import ru.t1.nour.microservice.model.enums.TransactionStatus;
import ru.t1.nour.microservice.model.enums.TransactionType;
import ru.t1.nour.microservice.repository.AccountRepository;
import ru.t1.nour.microservice.repository.CardRepository;
import ru.t1.nour.microservice.service.TransactionService;
import ru.t1.nour.microservice.service.impl.kafka.KafkaEventProducer;
import ru.t1.nour.microservice.web.CreditApiFacade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final KafkaEventProducer kafkaEventProducer;

    private final CreditApiFacade creditApiFacade;

    private final AccountRepository accountRepository;

    private final CardRepository cardRepository;

    @Transactional
    public void performTransaction(TransactionEventDTO event){
        Account account = accountRepository.findById(event.getAccountId()).orElseThrow(
                () -> new RuntimeException("Account with ID " + event.getAccountId() + " is not found.")
        );

        Card card = cardRepository.findById(event.getCardId()).orElseThrow(
                () -> new RuntimeException("Card with ID " + event.getCardId() + " is not found.")
        );

        if(cardIsNotAvailable(card)){
            log.error("The transaction on the card with ID {} was impossible due to the account being blocked or closed.", card.getCardId());
            return;
        }

        switch(event.getType()){
            case DEPOSIT -> account = depositMoney(account, event.getAmount());
            case WITHDRAW -> account = withdrawMoney(account, event.getAmount());
        }

        if (event.getType() == TransactionType.DEPOSIT && account.getIsRecalc())
            attemptScheduledPayment(account);
    }

    private Account depositMoney(Account account, BigDecimal amount){
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    private Account withdrawMoney(Account account, BigDecimal amount){
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    private boolean cardIsNotAvailable(Card card){
        return card.getStatus() == CardStatus.BLOCKED || card.getStatus() == CardStatus.CLOSED;
    }

    @Transactional
    private void attemptScheduledPayment(Account account) {
        log.info("Attempting scheduled payment for credit account {}", account.getId());

        // 1. ЗАПРАШИВАЕМ ИНФОРМАЦИЮ У ИСТОЧНИКА ПРАВДЫ (МС-3)
        Optional<NextCreditPaymentDTO> nextPaymentOpt = creditApiFacade.getNextPayment(account.getClientId());

        if (nextPaymentOpt.isEmpty()) {
            log.info("No scheduled payments found for client {}. Skipping.", account.getClientId());
            return;
        }

        NextCreditPaymentDTO nextPayment = nextPaymentOpt.get();

        // 2. ПРОВЕРЯЕМ УСЛОВИЯ ИЗ ТЗ

        // "наступил день платежа"
        boolean isPaymentDay = LocalDate.now().isEqual(ChronoLocalDate.from(nextPayment.getPaymentDate())) ||
                LocalDate.now().isAfter(ChronoLocalDate.from(nextPayment.getPaymentDate()));

        if (!isPaymentDay) {
            log.info("Payment day for client {} has not yet arrived. Skipping.", account.getClientId());
            return;
        }

        // "на балансе хватает средств"
        if (account.getBalance().compareTo(nextPayment.getAmount()) >= 0) {
            // Средств ДОСТАТОЧНО -> инициируем списание
            log.info("Sufficient funds. Debiting monthly payment...");

            // Отправляем событие на списание (как и раньше)
            TransactionEventDTO withdrawEvent = new TransactionEventDTO();
            Card card = cardRepository.findByAccount_Id(account.getId()).orElseThrow(
                    () -> new RuntimeException("Card is not found for account with ID " + account.getId())
            );

            withdrawEvent.setCardId(card.getCardId());
            withdrawEvent.setAmount(nextPayment.getAmount());
            withdrawEvent.setAccountId(account.getId());
            withdrawEvent.setTimestamp(LocalDateTime.now());
            withdrawEvent.setType(TransactionType.WITHDRAW);
            withdrawEvent.setStatus(TransactionStatus.PROCESSING);
            withdrawEvent.setEventType("NEW_TRANSACTION");

            kafkaEventProducer.sendTransaction(withdrawEvent);

            // И! Отправляем событие в МС-3, что платеж УСПЕШНО ПРОШЕЛ,
            // чтобы он мог обновить статус своего PaymentRegistry.
            // Например, в топик `credit_payment_results`.

        } else {
            // Средств НЕДОСТАТОЧНО
            log.warn("Insufficient funds for scheduled payment on account {}", account.getId());

            // Отправляем событие в МС-3, что платеж ПРОВАЛИЛСЯ,
            // чтобы он пометил свой PaymentRegistry как `expired = true`.
        }
    }
}
