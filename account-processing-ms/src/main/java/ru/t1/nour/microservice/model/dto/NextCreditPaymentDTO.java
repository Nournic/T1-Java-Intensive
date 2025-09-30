package ru.t1.nour.microservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NextCreditPaymentDTO {
    private Long paymentRegistryId;

    private LocalDateTime paymentDate;

    private BigDecimal amount;

    private Boolean expired;
}
