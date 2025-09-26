package ru.t1.nour.microservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;
import ru.t1.nour.microservice.model.enums.AccountStatus;
import ru.t1.nour.microservice.model.enums.TransactionStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account extends AbstractPersistable<Long> {
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "interest_rate", precision = 19, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "is_recalc", nullable = false)
    private Boolean isRecalc;

    @Column(name = "card_exist", nullable = false)
    private Boolean cardExist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus accountStatus;
}
