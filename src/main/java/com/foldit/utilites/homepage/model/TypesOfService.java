package com.foldit.utilites.homepage.model;

public enum TypesOfService {

    WASHING("WASHING"),
    IRONING("IRONING"),
    BOTH("WASHING & IRONING");

    private final String value;

    TypesOfService(String value) {
        this.value=value;
    }

}
