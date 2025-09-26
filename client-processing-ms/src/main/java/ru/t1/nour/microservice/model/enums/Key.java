package ru.t1.nour.microservice.model.enums;

public enum Key {
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

    Key(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
