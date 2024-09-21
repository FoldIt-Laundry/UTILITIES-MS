package com.foldit.utilites.shopadmin.model;

import java.util.Map;

public class AddOrderQuantityRequest {
    private String adminId;
    private String orderId;
    private Map<String,Double> serviceIdVsServiceQuantity;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<String, Double> getServiceIdVsServiceQuantity() {
        return serviceIdVsServiceQuantity;
    }

    public void setServiceIdVsServiceQuantity(Map<String, Double> serviceIdVsServiceQuantity) {
        this.serviceIdVsServiceQuantity = serviceIdVsServiceQuantity;
    }
}
