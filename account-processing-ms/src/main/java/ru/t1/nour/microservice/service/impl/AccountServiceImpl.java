package ru.t1.nour.microservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.Account;
import ru.t1.nour.microservice.model.Card;
import ru.t1.nour.microservice.model.dto.kafka.CardEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.model.enums.AccountStatus;
import ru.t1.nour.microservice.model.enums.CardStatus;
import ru.t1.nour.microservice.model.enums.PaymentSystem;
import ru.t1.nour.microservice.repository.AccountRepository;
import ru.t1.nour.microservice.repository.CardRepository;
import ru.t1.nour.microservice.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    @Value("app.credit.interest_rate")
    private final BigDecimal DEFAULT_INTEREST_RATE;

    @Override
    public void registerNewAccount(ClientProductEventDTO event) {
        if(accountRepository.existsByClientIdAndProductId(event.getClientId(), event.getProductId()))
            throw new RuntimeException("Account with clientId" + event.getClientId() + " and productId " + event.getProductId()
                    + " already exists");

        log.info("Start create new account by event: {}", event);
        Account newAccount = new Account();
        newAccount.setClientId(event.getClientId());
        newAccount.setProductId(event.getProductId());
        newAccount.setBalance(new BigDecimal(0));
        newAccount.setCardExist(false);

        boolean isCreditCard = isCreditCard(event.getProductKey());
        newAccount.setInterestRate(isCreditCard ? DEFAULT_INTEREST_RATE : BigDecimal.ZERO);
        newAccount.setIsRecalc(isCreditCard);

        newAccount.setAccountStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(newAccount);
        log.info("New account was successfully create: {}", savedAccount);
    }

    private boolean isCreditCard(String productKey){
        return List.of("IPO", "PC", "AC").contains(productKey);
    }

    @Override
    public void registerNewCard(CardEventDTO event) {
        Account account = accountRepository.findById(event.getAccountId()).orElseThrow(
                () -> new RuntimeException("Account with ID " + event.getAccountId() + " is not exists")
        );

        if(account.getAccountStatus() == AccountStatus.BLOCKED)
            throw new RuntimeException("Account with ID " + account.getId() + " is currently blocked. Can't create card.");

        Card newCard = new Card();
        newCard.setAccount(account);
        newCard.setCardId(0L); //TODO
        newCard.setStatus(CardStatus.INACTIVE);
        newCard.setPaymentSystem(PaymentSystem.valueOf(event.getPaymentSystem()));

        Card savedCard = cardRepository.save(newCard);
        log.info("New card was successfully created. Card: {}", savedCard);
    }
}
