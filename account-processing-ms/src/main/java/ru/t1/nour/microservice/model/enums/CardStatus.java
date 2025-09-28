package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CardStatus {
    INACTIVE("INACTIVE"),
    ACTIVE("ACTIVE"),
    FROZEN("FROZEN"),
    BLOCKED("BLOCKED"),
    EXPIRED("EXPIRED"),
    CLOSED("CLOSED");

    private final String value;
}
