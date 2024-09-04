package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "OrderDetails")
public class OrderDetails {
    private String id;
    private String userId;
    private String storeId;
    private OrderAddressDetails userAddress;
    private OrderAddressDetails shopAddress;
    private List<Services> addedService;
    private String workflowStatus;
    private List<WorkflowTransitionDetails> auditForWorkflowChanges;
    private String orderDeliveryTimeStamp;
    private String orderOrderedTimeStamp;
    private CostStructure billDetails;
    private String couponId;
    private Double timeLeftForDelivery = 8.00;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public OrderAddressDetails getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(OrderAddressDetails userAddress) {
        this.userAddress = userAddress;
    }

    public OrderAddressDetails getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(OrderAddressDetails shopAddress) {
        this.shopAddress = shopAddress;
    }

    public List<Services> getAddedService() {
        return addedService;
    }

    public void setAddedService(List<Services> addedService) {
        this.addedService = addedService;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public List<WorkflowTransitionDetails> getAuditForWorkflowChanges() {
        return auditForWorkflowChanges;
    }

    public void setAuditForWorkflowChanges(List<WorkflowTransitionDetails> auditForWorkflowChanges) {
        this.auditForWorkflowChanges = auditForWorkflowChanges;
    }

    public String getOrderDeliveryTimeStamp() {
        return orderDeliveryTimeStamp;
    }

    public void setOrderDeliveryTimeStamp(String orderDeliveryTimeStamp) {
        this.orderDeliveryTimeStamp = orderDeliveryTimeStamp;
    }

    public String getOrderOrderedTimeStamp() {
        return orderOrderedTimeStamp;
    }

    public void setOrderOrderedTimeStamp(String orderOrderedTimeStamp) {
        this.orderOrderedTimeStamp = orderOrderedTimeStamp;
    }

    public CostStructure getBillDetails() {
        return billDetails;
    }

    public void setBillDetails(CostStructure billDetails) {
        this.billDetails = billDetails;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public Double getTimeLeftForDelivery() {
        return timeLeftForDelivery;
    }

    public void setTimeLeftForDelivery(Double timeLeftForDelivery) {
        this.timeLeftForDelivery = timeLeftForDelivery;
    }
}
