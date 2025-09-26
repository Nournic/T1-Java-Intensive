package ru.t1.nour.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.t1.nour.microservice.model.PaymentRegistry;

import java.math.BigDecimal;

public interface PaymentRegistryRepository extends JpaRepository<PaymentRegistry, Long> {
    @Query("SELECT SUM(pr.debtAmount) FROM PaymentRegistry pr " +
            "WHERE pr.productRegistry.clientId = :clientId")
    BigDecimal getTotalDebtAmountByClientId(Long clientId);

    @Query("SELECT COUNT(p) > 0 FROM PaymentRegistry p " +
            "WHERE p.productRegistry.clientId = :clientId AND p.expired = true")
    boolean existsExpiredPaymentsForClient(Long clientId);
}