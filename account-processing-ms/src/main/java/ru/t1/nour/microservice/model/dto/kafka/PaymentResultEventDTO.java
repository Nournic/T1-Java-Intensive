package ru.t1.nour.microservice.model.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.nour.microservice.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentResultEventDTO {
    UUID transactionId;
    UUID paymentRegistryId;
    Long productRegistryId;
    BigDecimal amountPaid;
    private TransactionStatus status;
    private String failureReason;
    private String eventType;
}
