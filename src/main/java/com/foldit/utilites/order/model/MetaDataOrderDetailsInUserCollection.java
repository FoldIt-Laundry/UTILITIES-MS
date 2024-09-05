package com.foldit.utilites.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "UserDetails")
public class MetaDataOrderDetailsInUserCollection {
    @Id
    private String id;
    private Double orderCost;
    private String orderTimeStamp;
    private String userLocation;

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(Double orderCost) {
        this.orderCost = orderCost;
    }

    public String getOrderTimeStamp() {
        return orderTimeStamp;
    }

    public void setOrderTimeStamp(String orderTimeStamp) {
        this.orderTimeStamp = orderTimeStamp;
    }
}
