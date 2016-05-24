package com.example.tejas.finalv2.enums;

/**
 * Created by tejas on 2015/08/21.
 */
public enum LocationSenders {
    ALICE("Alice"),
    BOB("Bob"),
    ME("Me");

    String name;

    LocationSenders(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}