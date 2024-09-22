package com.foldit.utilites.order.model;

public enum WorkflowStatus {
    ORDER_PLACED(1),
    PENDING_WORKER_APPROVAL(2),
    ACCEPTED(3),
    ASSIGNED_FOR_RIDER_PICKUP(4),
    ORDER_PICKED_UP(5),
    IN_STORE(6),
    MAGIC_IN_PROGRESS(7),
    READY_FOR_DELIVERY(8),
    OUT_FOR_DELIVERY(9),
    DELIVERED(10);


    public final int value;


    WorkflowStatus(int value) {
        this.value = value;
    }
}
