package ru.t1.nour.microservice.model.dto.kafka.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {
    SUCCEEDED("SUCCEEDED"),
    FAILED("FAILED");

    private final String value;
}
