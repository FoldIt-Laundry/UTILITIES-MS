package com.foldit.utilites.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "UserDetails")
public class UserDetails {

    private String id;
    private String name;
    private String mobileNumber;
    private List<UserLocation> locations;
    private List<OrderHistoryDetailsInUserProfile> orderHistory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public List<UserLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<UserLocation> locations) {
        this.locations = locations;
    }

    public List<OrderHistoryDetailsInUserProfile> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<OrderHistoryDetailsInUserProfile> orderHistory) {
        this.orderHistory = orderHistory;
    }
}