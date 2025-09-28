package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Status {
    ACTIVE("ACTIVE"),
    CLOSED("CLOSED"),
    BLOCKED("BLOCKED"),
    ARRESTED("ARRESTED");

    private final String value;
}
