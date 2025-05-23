package com.foldit.utilites.worker.model;

import com.foldit.utilites.order.model.OrderDetails;

public class ApproveOrderRequest {
    private String workerId;
    private String orderId;
    private String storeId;


    public String getWorkerId() {
        return workerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
}
