package ru.t1.nour.microservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "product_registries")
public class ProductRegistry extends AbstractPersistable<Long> {
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "interest_rate", precision = 19, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "month_count")
    private int monthCount;

}
