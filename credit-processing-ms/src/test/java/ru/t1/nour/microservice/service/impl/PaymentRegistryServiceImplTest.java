package ru.t1.nour.microservice.service.impl;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.nour.microservice.mapper.PaymentRegistryMapper;
import ru.t1.nour.microservice.model.PaymentRegistry;
import ru.t1.nour.microservice.model.ProductRegistry;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.model.dto.kafka.PaymentResultEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.enums.PaymentStatus;
import ru.t1.nour.microservice.repository.PaymentRegistryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRegistryServiceImplTest {
    @Mock
    private PaymentRegistryRepository paymentRegistryRepository;

    @Mock
    private PaymentRegistryMapper paymentRegistryMapper;

    @InjectMocks
    private PaymentRegistryServiceImpl paymentRegistryService;

    @Captor
    private ArgumentCaptor<List<PaymentRegistry>> scheduleCaptor;

    @Test
    void should_createCorrectPaymentSchedule_forAnnuityCredit() {
        // --- ARRANGE ---
        // 1. Готовим исходные данные для кредита
        var productRegistry = new ProductRegistry();
        ReflectionTestUtils.setField(productRegistry, "id", 1L);
        productRegistry.setAmount(new BigDecimal("100000.00")); // 100 000
        productRegistry.setInterestRate(new BigDecimal("0.12")); // 12% годовых
        productRegistry.setMonthCount(12); // на 12 месяцев
        productRegistry.setOpenDate(LocalDateTime.of(2025, 1, 15, 10, 0));

        // 2. Для `saveAll` нам не нужно настраивать `thenReturn`,
        // так как метод возвращает `void`. Mockito справится по умолчанию.

        // --- ACT ---
        paymentRegistryService.createPaymentSchedule(productRegistry);

        // --- ASSERT ---
        // 1. "Ловим" список, который был передан в `saveAll`
        verify(paymentRegistryRepository).saveAll(scheduleCaptor.capture());
        List<PaymentRegistry> capturedSchedule = scheduleCaptor.getValue();

        // 2. Проверяем общие параметры графика
        assertThat(capturedSchedule).isNotNull();
        assertThat(capturedSchedule.toArray()).hasSize(12); // Должно быть 12 платежей

        // 3. Проверяем первый платеж
        PaymentRegistry firstPayment = capturedSchedule.get(0);
        assertThat(firstPayment.getPaymentDate().toLocalDate()).isEqualTo(LocalDate.of(2025, 2, 15));
        // Для 100000 на 12 мес под 12% годовых (1% в мес) аннуитетный платеж = 8884.88
        assertThat(firstPayment.getAmount()).isEqualByComparingTo("8884.88");
        // Проценты за первый месяц: 100000 * 0.01 = 1000.00
        assertThat(firstPayment.getInterestRateAmount()).isEqualByComparingTo("1000.00");
        // Погашение основного долга: 8884.88 - 1000.00 = 7884.88
        assertThat(firstPayment.getDebtAmount()).isEqualByComparingTo("7884.88");

        // 4. Проверяем последний платеж (особенно важна коррекция)
        PaymentRegistry lastPayment = capturedSchedule.get(11);
        assertThat(lastPayment.getPaymentDate().toLocalDate()).isEqualTo(LocalDate.of(2026, 1, 15));

        // Сумма основного долга и процентов в последнем платеже должна быть равна самому платежу
        assertThat(lastPayment.getDebtAmount().add(lastPayment.getInterestRateAmount()))
                .isEqualByComparingTo(lastPayment.getAmount());

        // 5. Проверяем, что весь долг погашен
        BigDecimal totalDebtPaid = capturedSchedule.stream()
                .map(PaymentRegistry::getDebtAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Сумма всех погашений основного долга должна быть равна исходной сумме кредита
        assertThat(totalDebtPaid.setScale(2, RoundingMode.HALF_UP))
                .isEqualByComparingTo(productRegistry.getAmount());
    }

    @Test
    void should_returnNextPaymentDto_when_unpaidPaymentExists() {
        // --- ARRANGE ---
        long clientId = 123L;

        // 1. Создаем "ответ" от репозитория
        var foundPayment = new PaymentRegistry();
        ReflectionTestUtils.setField(foundPayment, "id", 1L);
        // ... можно задать и другие поля, если они используются в маппере

        // 2. Создаем финальный DTO, который якобы вернет маппер
        var expectedDto = new NextCreditPaymentDTO();
        expectedDto.setPaymentRegistryId(1L);

        // 3. "Обучаем" моки
        // "Когда кто-то вызовет этот длинный метод с clientId, верни наш платеж"
        when(paymentRegistryRepository.findFirstByProductRegistryClientIdAndExpiredIsFalseOrderByPaymendDateAsc(clientId))
                .thenReturn(Optional.of(foundPayment));

        // "Когда маппер вызовут с нашим платежом, верни наш DTO"
        when(paymentRegistryMapper.toNextCreditPaymentDTO(foundPayment)).thenReturn(expectedDto);


        // --- ACT ---
        NextCreditPaymentDTO actualDto = paymentRegistryService.findNextUnpaidPayment(clientId);


        // --- ASSERT ---
        // 1. Проверяем, что вернулся правильный DTO
        assertThat(actualDto).isNotNull();
        assertThat(actualDto.getPaymentRegistryId()).isEqualTo(1L);

        // 2. Проверяем, что зависимости были вызваны
        verify(paymentRegistryRepository).findFirstByProductRegistryClientIdAndExpiredIsFalseOrderByPaymendDateAsc(clientId);
        verify(paymentRegistryMapper).toNextCreditPaymentDTO(foundPayment);
    }

    @Test
    void should_throwResourceNotFoundException_when_noUnpaidPaymentExists() {
        // --- ARRANGE ---
        long clientIdWithNoPayments = 404L;

        // "Обучаем" репозиторий возвращать "ничего"
        when(paymentRegistryRepository.findFirstByProductRegistryClientIdAndExpiredIsFalseOrderByPaymendDateAsc(clientIdWithNoPayments))
                .thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Проверяем, что был выброшен правильный тип исключения
        assertThatThrownBy(() -> paymentRegistryService.findNextUnpaidPayment(clientIdWithNoPayments))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payments are not found.");

        // Убеждаемся, что до маппера дело не дошло
        verify(paymentRegistryMapper, never()).toNextCreditPaymentDTO(any());
    }
    @Test
    void should_expirePayment_when_eventStatusIsFailed() {
        // --- ARRANGE ---
        long paymentRegistryId = 1L;
        var event = new PaymentResultEventDTO();
        event.setPaymentRegistryId(paymentRegistryId);
        event.setStatus(PaymentStatus.FAILED);

        var payment = new PaymentRegistry();
        payment.setExpired(false); // Изначально платеж не просрочен

        when(paymentRegistryRepository.findById(paymentRegistryId)).thenReturn(Optional.of(payment));

        // --- ACT ---
        paymentRegistryService.performPaymentEvent(event);

        // --- ASSERT ---
        // "Ловим" объект, переданный в save, и проверяем его
        ArgumentCaptor<PaymentRegistry> captor = ArgumentCaptor.forClass(PaymentRegistry.class);
        verify(paymentRegistryRepository).save(captor.capture());
        PaymentRegistry savedPayment = captor.getValue();

        assertThat(savedPayment.getExpired()).isTrue(); // Убеждаемся, что платеж помечен как просроченный
    }

    @Test
    void should_updatePaymentDate_when_paymentIsSuccessful_and_notFullRepayment() {
        // --- ARRANGE ---
        long paymentRegistryId = 1L;
        long productRegistryId = 10L;

        var event = new PaymentResultEventDTO();
        event.setPaymentRegistryId(paymentRegistryId);
        event.setStatus(PaymentStatus.SUCCEEDED);
        event.setAmountPaid(new BigDecimal("1000.00"));

        var productRegistry = new ProductRegistry();
        ReflectionTestUtils.setField(productRegistry, "id", productRegistryId);

        var payment = new PaymentRegistry();
        payment.setProductRegistry(productRegistry);
        payment.setPaymentDate(null); // Изначально дата оплаты null

        when(paymentRegistryRepository.findById(paymentRegistryId)).thenReturn(Optional.of(payment));
        // Имитируем, что общий долг БОЛЬШЕ, чем сумма платежа
        when(paymentRegistryRepository.findTotalRemainingDebtByProductRegistryId(productRegistryId))
                .thenReturn(new BigDecimal("5000.00"));

        // --- ACT ---
        paymentRegistryService.performPaymentEvent(event);

        // --- ASSERT ---
        // Проверяем, что была вызвана только логика стандартного платежа
        ArgumentCaptor<PaymentRegistry> captor = ArgumentCaptor.forClass(PaymentRegistry.class);
        verify(paymentRegistryRepository).save(captor.capture());

        // Убеждаемся, что у этого ОДНОГО платежа проставилась дата
        assertThat(captor.getValue().getPaymentDate()).isNotNull();

        // Убеждаемся, что логика полного погашения НЕ вызывалась
        verify(paymentRegistryRepository, never()).findAllByProductRegistryIdAndPayedAtIsNull(anyLong());
    }

    @Test
    void should_updateAllUnpaidPayments_when_paymentIsSuccessful_and_isFullRepayment() {
        // --- ARRANGE ---
        long paymentRegistryId = 1L;
        long productRegistryId = 10L;
        var now = LocalDateTime.now();

        var event = new PaymentResultEventDTO();
        event.setPaymentRegistryId(paymentRegistryId);
        event.setStatus(PaymentStatus.SUCCEEDED);
        event.setAmountPaid(new BigDecimal("10000.00"));

        var productRegistry = new ProductRegistry();
        ReflectionTestUtils.setField(productRegistry, "id", productRegistryId);

        var currentPayment = new PaymentRegistry();
        currentPayment.setProductRegistry(productRegistry);

        // Создаем список "неоплаченных" платежей, который вернет репозиторий
        var unpaidPayment1 = new PaymentRegistry();
        var unpaidPayment2 = new PaymentRegistry();
        List<PaymentRegistry> unpaidPayments = List.of(unpaidPayment1, unpaidPayment2);

        when(paymentRegistryRepository.findById(paymentRegistryId)).thenReturn(Optional.of(currentPayment));
        // Имитируем, что общий долг РАВЕН сумме платежа
        when(paymentRegistryRepository.findTotalRemainingDebtByProductRegistryId(productRegistryId))
                .thenReturn(event.getAmountPaid());
        when(paymentRegistryRepository.findAllByProductRegistryIdAndPayedAtIsNull(productRegistryId))
                .thenReturn(unpaidPayments);

        // --- ACT ---
        paymentRegistryService.performPaymentEvent(event);

        // --- ASSERT ---
        // 1. Проверяем, что была вызвана логика поиска всех неоплаченных платежей
        verify(paymentRegistryRepository).findAllByProductRegistryIdAndPayedAtIsNull(productRegistryId);

        // 2. Убеждаемся, что у КАЖДОГО из неоплаченных платежей проставилась дата
        assertThat(unpaidPayment1.getPaymentDate()).isNotNull();
        assertThat(unpaidPayment2.getPaymentDate()).isNotNull();

        // 3. (Опционально) Проверяем, что .save() не вызывался, т.к. изменения происходят
        // на "живых" Hibernate-сущностях внутри транзакции.
        // Если у вас нет @Transactional на методе, то здесь должна быть проверка saveAll.
        // verify(paymentRegistryRepository).saveAll(unpaidPayments);
    }

    @Test
    void should_throwResourceNotFoundException_when_paymentRegistryNotFound() {
        // --- ARRANGE ---
        long nonExistentId = 99L;
        var event = new PaymentResultEventDTO();
        event.setPaymentRegistryId(nonExistentId);
        event.setStatus(PaymentStatus.SUCCEEDED);

        when(paymentRegistryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> paymentRegistryService.performPaymentEvent(event))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
