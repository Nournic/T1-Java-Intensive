package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentSystem {
    VISA("VISA"),
    MASTERCARD("MASTERCARD"),
    MIR("MIR"),
    UNION_PAY("UNION_PAY");

    private final String value;
}
