package com.foldit.utilites.store.model;

public enum SlotsSegregation {
    MORNING(1),
    AFTERNOON(2),
    EVENING(3);

    private int value;

    SlotsSegregation(int value) {
        this.value =value;
    }
}
