package com.foldit.utilites.store.model;

public class AvailableTimeSlotsForScheduledPickupRequest {
    private String userId;
    private String mobileNumber;
    private String shopOpeningTime;
    private String shopClosingTime;

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShopOpeningTime() {
        return shopOpeningTime;
    }

    public void setShopOpeningTime(String shopOpeningTime) {
        this.shopOpeningTime = shopOpeningTime;
    }

    public String getShopClosingTime() {
        return shopClosingTime;
    }

    public void setShopClosingTime(String shopClosingTime) {
        this.shopClosingTime = shopClosingTime;
    }
}
