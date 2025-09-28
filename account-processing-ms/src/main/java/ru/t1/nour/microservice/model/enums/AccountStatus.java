package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AccountStatus {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    FROZEN("FROZEN"),
    ARRESTED("ARRESTED"),
    BLOCKED("BLOCKED"),
    CLOSED("CLOSED");

    private final String value;
}
