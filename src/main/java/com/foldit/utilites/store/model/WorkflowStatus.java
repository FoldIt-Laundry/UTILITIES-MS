package com.foldit.utilites.store.model;

public enum WorkflowStatus {
    ORDER_PLACED(1), //U  User, Notification to be sent to the workerIds
    ACCEPTED(2),  //U  Worker MArk + Assigned to delivery boy
    PICKED_UP(3), // U, Rider <-> OtpVerification
    IN_STORE(4), // Worker mark order received
    READY_FOR_DELIVERY(5), // R,W,A   Worker , Worker -> assign to Rider
    OUT_FOR_DELIVERY(6), // U,  Rider -> Marks it
    DELIVERED(7); // U  , Rider -> otp from user then finish

    public final int value;


    WorkflowStatus(int value) {
        this.value = value;
    }
}
