package com.foldit.utilites.constant;

public interface OrderRelatedConstant {

    String ORDER_UPDATE = "Order Update";

    String USER_UPDATE_ORDER_PLACED  = "Order Placed";
    String USER_UPDATE_ORDER_ACCEPTED  = "Order Accepted, Your order will be picked up soon";
    String USER_UPDATE_MAGIC_IN_PROGRESS  = "Order has reached store and magic has been started";

    String WORKER_ORDER_RECEIVED_REQUEST = "Order Received, Accept it";

    String RIDER_ORDER_ASSIGNED_FOR_PICKUP = "Order Received, Get Ready to pickup the order";
    String RIDER_ORDER_READY_FOR_DELIVERY = "Order is ready for delivery";

    String ADMIN_ORDER_RECEIVED_REQUEST = "Order Received, Ask worker to accept it";
    String ADMIN_ORDER_ACCEPTED_REQUEST_UPDATE = "Order Accepted, Worker has accepted the order";
    String ADMIN_ORDER_WORK_IN_PROGRESS_REQUEST_UPDATE = "Order Reached Store, Worker has accepted the order and started working on the order";
    String ADMIN_ORDER_READY_FOR_DELIVERY = "Order has been completed by worker and assigned to the rider";

    String USER_UPDATE_ORDER_OUT_FOR_DELIVERY = "Order is out for delivery";

}
