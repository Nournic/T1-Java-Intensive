package ru.t1.nour.microservice.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProductKey {
    DC("DC"),
    CC("CC"),
    AC("AC"),
    IPO("IPO"),
    PC("PC"),
    PENS("PENS"),
    NS("NS"),
    INS("INS"),
    BS("BS");

    private final String value;
}
