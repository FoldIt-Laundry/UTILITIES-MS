package com.foldit.utilites.order.model;

public enum WorkflowStatus {
    ORDER_PLACED(1), //U  User -> , Notification to be admin + worker  ( ORDER MESSAGE + ORDER ID  )
    // Worker fetch order ( Unaccepted ) -> Accept -> Backend ( Time and date fill  ) + Order Mark + USer Due date
    // Worker fetch order ( Today's order + Time Wise ) ( Pending orders + sorted by date and time )
    PENDING_WORKER_APPROVAL(2),
    ACCEPTED(3),  //U  Worker MArk + Assigned to delivery boy
    // Rider ko text message pickup ke lie +Order status
    // Same otp everytime for pickup and drop

    PICKED_UP(4), // U, Rider <-> OtpVerification
    // service and price final
    // Api for final price calculation

    // Rider ko pickup wise otp verification ke lie
    // All orders for pickup :- Sort the data based on time and date  ??????
    // All order for drop :-

    // Payment Razorpay ??????????????????

    IN_STORE(5), // Worker mark order received
    // Rider update krdega order
    //

    READY_FOR_DELIVERY(5), // R,W,A   Worker , Worker -> assign to Rider
    // Worker mark krdega and will assign it to rider to available drop

    OUT_FOR_DELIVERY(6), // U,  Rider -> Marks it
    //

    DELIVERED(7); // U  ,


    public final int value;


    WorkflowStatus(int value) {
        this.value = value;
    }
}
