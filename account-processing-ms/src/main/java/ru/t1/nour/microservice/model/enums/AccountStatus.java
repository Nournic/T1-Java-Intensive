package ru.t1.nour.microservice.model.enums;

public enum AccountStatus {
    PENDING("PENDING"),
    ACTIVE("ACTIVE"),
    FROZEN("FROZEN"),
    ARRESTED("ARRESTED"),
    BLOCKED("BLOCKED"),
    CLOSED("CLOSED");

    private final String value;

    AccountStatus(String value) {
        this.value = value;
    }
}
