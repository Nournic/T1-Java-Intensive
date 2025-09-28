package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentType {
    SINGLE("SINGLE"),
    RECURRING("RECURRING"),
    P2P_TRANSFER("P2P_TRANSFER"),
    BILL_PAYMENT("BILL_PAYMENT"),
    AUTO_REFILL("AUTO_REFILL"),
    INSTALLMENT("INSTALLMENT");

    private final String value;
}
