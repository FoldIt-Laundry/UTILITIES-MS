package com.foldit.utilites.rider.model;

public enum RiderDeliveryTask {
    PICKUP(1),
    DROP(2);

    private Integer value;

    RiderDeliveryTask(Integer value) {
        this.value = value;
    }
}
