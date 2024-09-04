package com.foldit.utilites.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UserDetails")
public class OrderHistoryDetailsInUserProfile {
    private String id;
    private String orderTimeStamp;
    private Double orderCost;
    private Integer rating;
    private UserLocationShortInfo userLocation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderTimeStamp() {
        return orderTimeStamp;
    }

    public void setOrderTimeStamp(String orderTimeStamp) {
        this.orderTimeStamp = orderTimeStamp;
    }

    public Double getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(Double orderCost) {
        this.orderCost = orderCost;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public UserLocationShortInfo getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocationShortInfo userLocation) {
        this.userLocation = userLocation;
    }
}

