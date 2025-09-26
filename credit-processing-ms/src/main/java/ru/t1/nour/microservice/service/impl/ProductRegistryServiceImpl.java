package ru.t1.nour.microservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.model.ProductRegistry;
import ru.t1.nour.microservice.model.dto.ClientInfoResponse;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.repository.PaymentRegistryRepository;
import ru.t1.nour.microservice.repository.ProductRegistryRepository;
import ru.t1.nour.microservice.service.PaymentRegistryService;
import ru.t1.nour.microservice.service.ProductRegistryService;
import ru.t1.nour.microservice.web.ClientApiFacade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductRegistryServiceImpl implements ProductRegistryService {
    private final ProductRegistryRepository productRegistryRepository;

    private final PaymentRegistryRepository paymentRegistryRepository;

    private final PaymentRegistryService paymentRegistryService;

    private final ClientApiFacade clientApiFacade;

    private final BigDecimal LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS;

    private static final BigDecimal DEFAULT_INTEREST_RATE = BigDecimal.valueOf(1.15);

    public void createByEvent(ClientProductEventDTO event){
        Long clientId = event.getClientId();
        ClientInfoResponse client = clientApiFacade.getClientInfo(clientId);

        BigDecimal requestedAmount = event.getRequestedAmount();


        BigDecimal totalExistingDebt = paymentRegistryRepository.getTotalDebtAmountByClientId(clientId);

        if (totalExistingDebt == null)
            totalExistingDebt = BigDecimal.ZERO;

        BigDecimal newTotalDebt = totalExistingDebt.add(requestedAmount);

        if (newTotalDebt.compareTo(LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS) > 0) {
            log.warn("Credit REJECTED for client {}. Reason: Total limit exceeded.", clientId);
            return;
        }

        boolean hasOverduePayments = paymentRegistryRepository.existsExpiredPaymentsForClient(clientId);
        if (hasOverduePayments) {
            log.warn("Credit REJECTED for client {}. Reason: Client has overdue payments.", clientId);
            return;
        }

        log.info("Credit APPROVED for client {}.", clientId);
        openProductAndCreateSchedule(event);
    }

    private void openProductAndCreateSchedule(ClientProductEventDTO event) {
        ProductRegistry productRegistry = new ProductRegistry();

        productRegistry.setProductId(event.getProductId());
        productRegistry.setClientId(event.getClientId());
        productRegistry.setAccountId(0L);
        productRegistry.setOpenDate(LocalDateTime.now());
        productRegistry.setInterestRate(DEFAULT_INTEREST_RATE);
        productRegistry.setMonthCount(event.getMonthCount());

        ProductRegistry savedProductRegistry = productRegistryRepository.save(productRegistry);

        paymentRegistryService.createPaymentSchedule(savedProductRegistry);
    }
}
