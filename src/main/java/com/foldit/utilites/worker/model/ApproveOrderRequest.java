package com.foldit.utilites.worker.model;

import com.foldit.utilites.order.model.OrderDetails;

public class ApproveOrderRequest {
    private String workerId;
    private OrderDetails orderDetails;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public OrderDetails getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(OrderDetails orderDetails) {
        this.orderDetails = orderDetails;
    }
}
