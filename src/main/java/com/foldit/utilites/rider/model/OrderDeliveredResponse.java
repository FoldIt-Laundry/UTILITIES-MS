package com.foldit.utilites.rider.model;

public class OrderDeliveredResponse {

    public boolean orderDelivered;

    public OrderDeliveredResponse(boolean orderDelivered) {
        this.orderDelivered = orderDelivered;
    }

    public boolean isOrderDelivered() {
        return orderDelivered;
    }

    public void setOrderDelivered(boolean orderDelivered) {
        this.orderDelivered = orderDelivered;
    }
}
