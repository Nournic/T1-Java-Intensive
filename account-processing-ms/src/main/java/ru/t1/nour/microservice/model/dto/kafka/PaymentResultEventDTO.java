package ru.t1.nour.microservice.model.dto.kafka;

import ru.t1.nour.microservice.model.enums.TransactionStatus;

import java.math.BigDecimal;

public class PaymentResultEventDTO {
    Long transactionId;
    Long paymentRegistryId;
    Long productRegistryId;
    BigDecimal amountPaid;
    private TransactionStatus status;
    private String failureReason;
    private String eventType;
}
