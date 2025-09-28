package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TransactionStatus {
    ALLOWED("ALLOWED"),
    PROCESSING("PROCESSING"),
    COMPLETE("COMPLETE"),
    BLOCKED("BLOCKED"),
    CANCELLED("CANCELLED");

    private final String value;
}
