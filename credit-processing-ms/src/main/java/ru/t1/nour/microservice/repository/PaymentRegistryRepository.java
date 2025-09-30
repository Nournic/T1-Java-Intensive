package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.t1.nour.microservice.model.PaymentRegistry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {
    @Query("SELECT SUM(pr.debtAmount) FROM PaymentRegistry pr " +
            "WHERE pr.productRegistry.clientId = :clientId")
    BigDecimal getTotalDebtAmountByClientId(Long clientId);

    @Query("SELECT COUNT(p) > 0 FROM PaymentRegistry p " +
            "WHERE p.productRegistry.clientId = :clientId AND p.expired = true")
    boolean existsExpiredPaymentsForClient(Long clientId);

    Optional<PaymentRegistry> findFirstByProductRegistryClientIdAndExpiredIsFalseOrderByPaymendDateAsc(Long clientId);

    /**
     * Считает сумму всех debtAmount для всех еще НЕОПЛАЧЕННЫХ платежей
     * по конкретному кредитному договору (ProductRegistry).
     * @param productRegistryId ID кредитного договора
     * @return Общая сумма оставшегося основного долга.
     */
    @Query("SELECT SUM(pr.debtAmount) FROM PaymentRegistry pr " +
            "WHERE pr.productRegistry.id = :productRegistryId AND pr.payedAt IS NULL")
    BigDecimal findTotalRemainingDebtByProductRegistryId(Long productRegistryId);

    List<PaymentRegistry> findAllByProductRegistryIdAndPayedAtIsNull(Long productRegistryId);
}