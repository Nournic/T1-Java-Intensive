package ru.t1.nour.microservice.model.enums;

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

    private String name;

    ProductKey(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
