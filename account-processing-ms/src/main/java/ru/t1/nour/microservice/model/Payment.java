package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends AbstractPersistable<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_credit")
    private Boolean isCredit;

    @Column(name = "payed_at")
    private LocalDateTime payedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PaymentType type;
}
