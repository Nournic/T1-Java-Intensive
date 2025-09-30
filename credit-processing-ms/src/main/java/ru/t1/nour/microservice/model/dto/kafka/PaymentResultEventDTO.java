package ru.t1.nour.microservice.model.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.t1.nour.microservice.model.dto.kafka.enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultEventDTO {
    Long transactionId;
    Long paymentRegistryId;
    Long productRegistryId;
    BigDecimal amountPaid;
    private PaymentStatus status;
    private String failureReason;
    private String eventType;
}
