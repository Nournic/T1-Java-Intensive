package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_registries")
public class PaymentRegistry extends AbstractPersistable<Long> {
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "interest_rate_amount", precision = 19, scale = 2)
    private BigDecimal interestRateAmount;

    @Column(name = "debt_amount", precision = 19, scale = 2)
    private BigDecimal debtAmount;

    @Column(name = "expired")
    private Boolean expired;

    @Column(name = "payment_expiration_date")
    private LocalDateTime paymentExpirationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_registry_id")
    private ProductRegistry productRegistry;

}
