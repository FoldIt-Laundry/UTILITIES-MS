package com.foldit.utilites.constant;

public interface OrderRelatedConstant {

    // Order Status
    String ORDER_UPDATE = "Order Update";
    String ORDER_OUT_FOR_DELIVERY = "Order out for delivery";
    String ORDER_DELIVERED = "Order delivered";
    String ORDER_CANCELLED = "Order cancelled";
    String ORDER_RESCHEDULED = "Order rescheduled";

    // User Status
    String USER_UPDATE_ORDER_PLACED  = "Order Placed";
    String USER_UPDATE_ORDER_ACCEPTED  = "Order Accepted, Your order will be picked up soon";
    String USER_UPDATE_ORDER_OUT_FOR_PICKUP = "Order is out for pickup";
    String USER_UPDATE_MAGIC_IN_PROGRESS  = "Order has reached store and magic has been started";
    String USER_UPDATE_ORDER_PICKED_UP  = "Your order has been picked up";
    String USER_UPDATE_ORDER_CANCELLED = "Your order has been cancelled";
    String USER_UPDATE_ORDER_RESCHEDULED = "Your order has been rescheduled successfully";
    String USER_UPDATE_ORDER_OUT_FOR_DELIVERY  = "Please share otp %s to complete the delivery";
    String USER_UPDATE_ORDER_QUANTITY_DETAILS_UPDATED  = "Your total amount for the order is %s";
    String USER_UPDATE_ORDER_DELIVERED_SUCCESSFULLY  = "Order has been delivered successfully";


    // Worker Status
    String WORKER_ORDER_RECEIVED_REQUEST = "Order Received, Accept it";
    String WORKER_ORDER_RESCHEDULED_RECEIVED_REQUEST = "Order rescheduled request received, Accept it";

    // Rider Status
    String RIDER_ORDER_ASSIGNED_FOR_PICKUP = "Order Received, Get Ready to pickup the order";
    String RIDER_ORDER_READY_FOR_DELIVERY = "Order is ready for delivery";

    // Admin Status
    String ADMIN_ORDER_RECEIVED_REQUEST = "Order Received, Ask worker to accept it";
    String ADMIN_ORDER_ACCEPTED_REQUEST_UPDATE = "Order Accepted, Worker has accepted the order";
    String ADMIN_ORDER_ASSIGNED_RIDER_TO_PICKUP = "Order assigned to rider for pickup";
    String ADMIN_ORDER_WORK_IN_PROGRESS_REQUEST_UPDATE = "Order Reached Store, Worker has accepted the order and started working on the order";
    String ADMIN_ORDER_READY_FOR_DELIVERY = "Order has been completed by worker and assigned to the rider";
    String ADMIN_ORDER_RIDER_PICKED_UP_THE_ORDER = "Rider has picked up the order %s from the user";
    String ADMIN_ORDER_USER_CANCELLED_THE_ORDER = "User cancelled the order number: %s";
    String ADMIN_ORDER_USER_RESCHEDULED_THE_ORDER = "User has rescheduled the order number: %s";


}
