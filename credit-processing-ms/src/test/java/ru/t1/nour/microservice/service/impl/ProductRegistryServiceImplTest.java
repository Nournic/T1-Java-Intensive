package ru.t1.nour.microservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.model.ProductRegistry;
import ru.t1.nour.microservice.model.dto.ClientInfoResponse;
import ru.t1.nour.microservice.model.dto.kafka.ClientProductEventDTO;
import ru.t1.nour.microservice.repository.PaymentRegistryRepository;
import ru.t1.nour.microservice.repository.ProductRegistryRepository;
import ru.t1.nour.microservice.service.PaymentRegistryService;
import ru.t1.nour.microservice.service.impl.kafka.ProductEventProducer;
import ru.t1.nour.microservice.web.ClientApiFacade;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductRegistryServiceImplTest {
    @Mock
    private ProductEventProducer productEventProducer;
    @Mock
    private ProductRegistryRepository productRegistryRepository;
    @Mock
    private PaymentRegistryRepository paymentRegistryRepository;
    @Mock
    private PaymentRegistryService paymentRegistryService;
    @Mock
    private ClientApiFacade clientApiFacade;

    @InjectMocks
    private ProductRegistryServiceImpl productRegistryService;

    private final BigDecimal LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS = new BigDecimal("1000000.00");
    private final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("0.22");

    @BeforeEach
    void setUp() {
        // "Внедряем" значения в поля
        ReflectionTestUtils.setField(productRegistryService, "LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS", LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS);
        ReflectionTestUtils.setField(productRegistryService, "DEFAULT_INTEREST_RATE", DEFAULT_INTEREST_RATE);
    }

    @Test
    void should_approveCreditAndCreateSchedule_when_allChecksPass() {
        // --- ARRANGE ---
        long clientId = 1L;
        var event = new ClientProductEventDTO();
        event.setClientId(clientId);
        event.setMonthCount(12);
        event.setRequestedAmount(new BigDecimal("50000"));

        var clientInfo = new ClientInfoResponse();
        var savedProductRegistry = new ProductRegistry();

        // "Обучаем" моки для "happy path"
        when(clientApiFacade.getClientInfo(clientId)).thenReturn(clientInfo);
        // Текущий долг + запрашиваемый < лимита
        when(paymentRegistryRepository.getTotalDebtAmountByClientId(clientId)).thenReturn(new BigDecimal("100000"));
        // Нет просроченных платежей
        when(paymentRegistryRepository.existsExpiredPaymentsForClient(clientId)).thenReturn(false);
        when(productRegistryRepository.save(any(ProductRegistry.class))).thenReturn(savedProductRegistry);

        // --- ACT ---
        productRegistryService.createByEvent(event);

        // --- ASSERT ---
        // 1. Проверяем, что была попытка получить инфо о клиенте
        verify(clientApiFacade).getClientInfo(clientId);

        // 2. Проверяем, что было отправлено событие об одобрении
        verify(productEventProducer).sendProductEvent(event);

        // 3. Проверяем, что был сохранен новый кредитный продукт (ProductRegistry)
        verify(productRegistryRepository).save(any(ProductRegistry.class));

        // 4. Проверяем, что была вызвана логика создания графика платежей
        // и ей был передан именно тот объект, который вернул `save`
        verify(paymentRegistryService).createPaymentSchedule(savedProductRegistry);
    }

    @Test
    void should_rejectCredit_when_totalLimitExceeded() {
        // --- ARRANGE ---
        long clientId = 2L;
        var event = new ClientProductEventDTO();
        event.setClientId(clientId);
        event.setRequestedAmount(new BigDecimal("50000"));

        var clientInfo = new ClientInfoResponse();

        when(clientApiFacade.getClientInfo(clientId)).thenReturn(clientInfo);
        // Текущий долг + запрашиваемый > лимита
        when(paymentRegistryRepository.getTotalDebtAmountByClientId(clientId))
                .thenReturn(LIMIT_ON_TOTAL_AMOUNT_OF_CREDITS); // Уже на лимите

        // --- ACT ---
        productRegistryService.createByEvent(event);

        // --- ASSERT ---
        // Главная проверка: убедиться, что до одобряющих действий дело не дошло
        verify(productEventProducer, never()).sendProductEvent(any());
        verify(productRegistryRepository, never()).save(any());
        verify(paymentRegistryService, never()).createPaymentSchedule(any());

        // Проверяем, что проверки долга были
        verify(paymentRegistryRepository).getTotalDebtAmountByClientId(clientId);
        // А проверка на просрочку уже не нужна
        verify(paymentRegistryRepository, never()).existsExpiredPaymentsForClient(anyLong());
    }

    @Test
    void should_rejectCredit_when_clientHasOverduePayments() {
        // --- ARRANGE ---
        long clientId = 3L;
        var event = new ClientProductEventDTO();
        event.setClientId(clientId);
        event.setRequestedAmount(new BigDecimal("50000"));

        var clientInfo = new ClientInfoResponse();

        when(clientApiFacade.getClientInfo(clientId)).thenReturn(clientInfo);
        // Лимит по долгу проходит
        when(paymentRegistryRepository.getTotalDebtAmountByClientId(clientId)).thenReturn(new BigDecimal("100000"));
        // Но есть просрочка
        when(paymentRegistryRepository.existsExpiredPaymentsForClient(clientId)).thenReturn(true);

        // --- ACT ---
        productRegistryService.createByEvent(event);

        // --- ASSERT ---
        // Убеждаемся, что до одобряющих действий дело не дошло
        verify(productEventProducer, never()).sendProductEvent(any());
        verify(productRegistryRepository, never()).save(any());
        verify(paymentRegistryService, never()).createPaymentSchedule(any());

        // Проверяем, что обе проверки были выполнены
        verify(paymentRegistryRepository).getTotalDebtAmountByClientId(clientId);
        verify(paymentRegistryRepository).existsExpiredPaymentsForClient(clientId);
    }
}
