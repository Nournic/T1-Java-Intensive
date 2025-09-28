package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DocumentType {
    PASSPORT("PASSPORT"),
    INT_PASSPORT("INT_PASSPORT"),
    BIRTH_CERT("BIRTH_CERT");

    private final String value;
}
