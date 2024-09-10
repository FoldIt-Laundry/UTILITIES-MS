package com.foldit.utilites.store.model;

public enum WorkflowStatus {
    ORDER_PLACED, //U  User
    ACCEPTED,  //U  Worker MArk + Assigned to delivery boy
    PICKED_UP, // U, Rider <-> OtpVerification
    IN_STORE, // Worker mark order received
    READY_FOR_DELIVERY, // R,W,A   Worker , Worker -> assign to Rider
    OUT_FOR_DELIVERY, // U,  Rider -> Marks it
    DELIVERED; // U  , Rider -> otp from user then finish
}
