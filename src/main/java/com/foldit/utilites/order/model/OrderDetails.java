package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static com.foldit.utilites.helper.GenerateOtp.generate4DigitOtpCode;

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

    private Double rating;
    private Double timeLeftForDelivery = 8.00;
    private String checkOutOtp = generate4DigitOtpCode();

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCheckOutOtp() {
        return checkOutOtp;
    }

    public void setCheckOutOtp(String checkOutOtp) {
        this.checkOutOtp = checkOutOtp;
    }

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


    public Double getTimeLeftForDelivery() {
        return timeLeftForDelivery;
    }

    public void setTimeLeftForDelivery(Double timeLeftForDelivery) {
        this.timeLeftForDelivery = timeLeftForDelivery;
    }
}
