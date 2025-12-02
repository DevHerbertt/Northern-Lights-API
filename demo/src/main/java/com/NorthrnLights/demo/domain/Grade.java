package com.NorthrnLights.demo.domain;

public enum Grade {
    A_PLUS("A+"),
    A("A"),
    A_MINUS("A-"),
    B_PLUS("B+"),
    B("B"),
    B_MINUS("B-"),
    C_PLUS("C+"),
    C("C"),
    C_MINUS("C-"),
    D_PLUS("D+"),
    D("D"),
    F("F");

    private final String display;

    Grade(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
