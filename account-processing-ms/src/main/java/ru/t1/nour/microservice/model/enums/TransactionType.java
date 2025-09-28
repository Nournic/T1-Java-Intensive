package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionType {
    DEBIT("DEBIT"),
    CREDIT("CREDIT");

    private final String value;
}
