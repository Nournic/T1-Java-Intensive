package ru.t1.nour.microservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import ru.t1.nour.microservice.mapper.PaymentRegistryMapper;
import ru.t1.nour.microservice.model.PaymentRegistry;
import ru.t1.nour.microservice.model.ProductRegistry;
import ru.t1.nour.microservice.model.dto.NextCreditPaymentDTO;
import ru.t1.nour.microservice.model.dto.kafka.PaymentResultEventDTO;
import ru.t1.nour.microservice.model.dto.kafka.enums.PaymentStatus;
import ru.t1.nour.microservice.repository.PaymentRegistryRepository;
import ru.t1.nour.microservice.service.PaymentRegistryService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentRegistryServiceImpl implements PaymentRegistryService {
    private static final int CURRENCY_SCALE = 2; // 2 знака после запятой для денег
    private static final int CALCULATION_SCALE = 10; // Точность для промежуточных расчетов
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    private final PaymentRegistryRepository paymentRegistryRepository;

    private final PaymentRegistryMapper paymentRegistryMapper;

    /**
     * Создает и сохраняет полный график аннуитетных платежей для кредитного договора.
     * @param productRegistry Сущность кредитного договора, содержащая сумму, ставку и срок.
     */
    @Transactional
    public void createPaymentSchedule(ProductRegistry productRegistry) {
        BigDecimal creditAmount = productRegistry.getAmount();
        BigDecimal annualRate = productRegistry.getInterestRate(); // Годовая ставка, например, 0.22
        int numberOfMonths = productRegistry.getMonthCount();

        // Рассчитываем месячную процентную ставку (i)
        BigDecimal monthlyRate = annualRate.divide(MONTHS_IN_YEAR, CALCULATION_SCALE, RoundingMode.HALF_UP);

        // Рассчитываем размер аннуитетного платежа (A)
        BigDecimal annuityPayment = calculateAnnuityPayment(creditAmount, monthlyRate, numberOfMonths);
        log.info("Calculated annuity payment: {} for productRegistryId: {}", annuityPayment, productRegistry.getId());

        List<PaymentRegistry> schedule = new ArrayList<>();
        BigDecimal remainingDebt = creditAmount; // Остаток долга на начало
        LocalDate paymentDate = productRegistry.getOpenDate().toLocalDate().plusMonths(1); // Первый платеж через месяц

        for (int i = 1; i <= numberOfMonths; i++) {
            // Рассчитываем проценты за текущий месяц
            BigDecimal interestForMonth = remainingDebt.multiply(monthlyRate)
                    .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);

            // Рассчитываем погашение основного долга
            BigDecimal principalForMonth = annuityPayment.subtract(interestForMonth);

            // Корректировка для последнего платежа, чтобы закрыть долг в ноль из-за ошибок округления
            if (i == numberOfMonths) {
                principalForMonth = remainingDebt;
                annuityPayment = principalForMonth.add(interestForMonth);
            }

            // Создаем запись для графика
            PaymentRegistry payment = new PaymentRegistry();
            payment.setProductRegistry(productRegistry);
            payment.setPaymentDate(paymentDate.atStartOfDay()); // Дата планового платежа
            payment.setPaymentExpirationDate(paymentDate.plusDays(10).atStartOfDay()); // Срок оплаты (например, 10 дней)
            payment.setAmount(annuityPayment);
            payment.setInterestRateAmount(interestForMonth);
            payment.setDebtAmount(principalForMonth);
            payment.setExpired(false);

            schedule.add(payment);

            // Уменьшаем остаток долга и сдвигаем дату на следующий месяц
            remainingDebt = remainingDebt.subtract(principalForMonth);
            paymentDate = paymentDate.plusMonths(1);
        }

        paymentRegistryRepository.saveAll(schedule);
        log.info("Successfully created payment schedule with {} payments for productRegistryId: {}", schedule.size(), productRegistry.getId());
    }

    /**
     * Реализация формулы аннуитетного платежа.
     * А = S * [i * (1 + i)^n] / [(1 + i)^n - 1]
     */
    private BigDecimal calculateAnnuityPayment(BigDecimal creditAmount, BigDecimal monthlyRate, int numberOfMonths) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return creditAmount.divide(new BigDecimal(numberOfMonths), CURRENCY_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal onePlusRatePowered = onePlusRate.pow(numberOfMonths);

        BigDecimal numerator = monthlyRate.multiply(onePlusRatePowered);
        BigDecimal denominator = onePlusRatePowered.subtract(BigDecimal.ONE);

        BigDecimal ratio = numerator.divide(denominator, CALCULATION_SCALE, RoundingMode.HALF_UP);

        return creditAmount.multiply(ratio).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    public NextCreditPaymentDTO findNextUnpaidPayment(Long clientId){
        PaymentRegistry payment = paymentRegistryRepository
                .findFirstByProductRegistryClientIdAndExpiredIsFalseOrderByPaymendDateAsc(clientId)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Payments are not found.")
                );

        return paymentRegistryMapper.toNextCreditPaymentDTO(payment);

    }

    @Transactional
    public void performPaymentEvent(PaymentResultEventDTO event){
        PaymentRegistry paymentRegistry = paymentRegistryRepository.findById(event.getPaymentRegistryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Payment with ID " + event.getPaymentRegistryId() + " is not found.")
                );

        if(event.getStatus()== PaymentStatus.FAILED) {
            log.error("Transaction failed");
            paymentRegistry.setExpired(true);
            paymentRegistryRepository.save(paymentRegistry);
            return;
        }

        if(event.getStatus() == PaymentStatus.SUCCEEDED){
            log.info("Processing successful payment for payment {}", paymentRegistry.getId());

            Long productRegistryId = paymentRegistry.getProductRegistry().getId();

            BigDecimal totalRemainingDebt = paymentRegistryRepository.findTotalRemainingDebtByProductRegistryId(productRegistryId);
            if (totalRemainingDebt == null)
                totalRemainingDebt = BigDecimal.ZERO;


            if (event.getAmountPaid().setScale(2, RoundingMode.HALF_UP)
                    .compareTo(totalRemainingDebt.setScale(2, RoundingMode.HALF_UP)) == 0) {

                log.info("Full repayment detected for productRegistryId: {}", productRegistryId);

                List<PaymentRegistry> unpaidPayments = paymentRegistryRepository
                        .findAllByProductRegistryIdAndPayedAtIsNull(productRegistryId);

                LocalDateTime now = LocalDateTime.now();
                for (PaymentRegistry payment : unpaidPayments) {
                    payment.setPaymentDate(now);
                }

            } else {
                log.info("Standard monthly payment processed for paymentId: {}", paymentRegistry.getId());
                paymentRegistry.setPaymentDate(LocalDateTime.now());
            }
        }
    }
}
