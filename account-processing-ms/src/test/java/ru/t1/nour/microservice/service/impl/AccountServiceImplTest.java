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
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.enums.AccountStatus;
import ru.t1.nour.microservice.model.enums.CardStatus;
import ru.t1.nour.microservice.model.enums.PaymentSystem;
import ru.t1.nour.microservice.repository.AccountRepository;
import ru.t1.nour.microservice.repository.CardRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("15.5");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(accountService, "DEFAULT_INTEREST_RATE", DEFAULT_INTEREST_RATE);
    }

    @Test
    void should_createNonCreditAccount_when_productKeyIsNotCredit() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        event.setClientId(1L);
        event.setProductId(10L);
        event.setProductKey("DC"); // НЕ кредитный продукт

        when(accountRepository.existsByClientIdAndProductId(1L, 10L)).thenReturn(false);
        // Имитируем, что save возвращает свой аргумент
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        accountService.registerNewAccount(event);

        // ASSERT
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account savedAccount = captor.getValue();

        assertThat(savedAccount.getClientId()).isEqualTo(1L);
        assertThat(savedAccount.getIsRecalc()).isFalse();
        assertThat(savedAccount.getInterestRate()).isEqualTo(BigDecimal.ZERO);
        assertThat(savedAccount.getAccountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void should_createCreditAccount_when_productKeyIsCredit() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        event.setClientId(2L);
        event.setProductId(20L);
        event.setProductKey("PC"); // Кредитный продукт

        when(accountRepository.existsByClientIdAndProductId(2L, 20L)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        accountService.registerNewAccount(event);

        // ASSERT
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account savedAccount = captor.getValue();

        assertThat(savedAccount.getIsRecalc()).isTrue();
        assertThat(savedAccount.getInterestRate()).isEqualTo(DEFAULT_INTEREST_RATE);
    }

    @Test
    void should_throwException_when_registeringExistingAccount() {
        // ARRANGE
        var event = new ClientProductEventDTO();
        event.setClientId(1L);
        event.setProductId(10L);

        when(accountRepository.existsByClientIdAndProductId(1L, 10L)).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> accountService.registerNewAccount(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(accountRepository, never()).save(any());
    }

    // --- ТЕСТЫ ДЛЯ registerNewCard ---

    @Test
    void should_createCard_when_accountIsActive() {
        // ARRANGE
        var event = new CardEventDTO();
        event.setAccountId(1L);
        event.setPaymentSystem("VISA");

        var account = new Account();
        account.setAccountStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        accountService.registerNewCard(event);

        // ASSERT
        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(captor.capture());
        Card savedCard = captor.getValue();

        assertThat(savedCard.getAccount()).isEqualTo(account);
        assertThat(savedCard.getStatus()).isEqualTo(CardStatus.INACTIVE);
        assertThat(savedCard.getPaymentSystem()).isEqualTo(PaymentSystem.VISA);
    }

    @Test
    void should_throwException_when_creatingCardForNonExistentAccount() {
        // ARRANGE
        var event = new CardEventDTO();
        event.setAccountId(99L);

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> accountService.registerNewCard(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account with ID 99 is not exists");

        verify(cardRepository, never()).save(any());
    }

    @Test
    void should_throwException_when_creatingCardForBlockedAccount() {
        // ARRANGE
        var event = new CardEventDTO();
        event.setAccountId(1L);

        var blockedAccount = new Account();
        ReflectionTestUtils.setField(blockedAccount, "id", 1L); // Установим ID для сообщения об ошибке
        blockedAccount.setAccountStatus(AccountStatus.BLOCKED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(blockedAccount));

        // ACT & ASSERT
        assertThatThrownBy(() -> accountService.registerNewCard(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Account with ID 1 is currently blocked. Can't create card.");

        verify(cardRepository, never()).save(any());
    }

}
