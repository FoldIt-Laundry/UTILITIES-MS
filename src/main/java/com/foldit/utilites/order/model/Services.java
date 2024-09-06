package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OrderDetails")
public class Services {
    private String serviceName;
    private String serviceId;
    private double clothesKgSize;
    private int numberOfIronedClothes;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public double getClothesKgSize() {
        return clothesKgSize;
    }

    public void setClothesKgSize(double clothesKgSize) {
        this.clothesKgSize = clothesKgSize;
    }

    public int getNumberOfIronedClothes() {
        return numberOfIronedClothes;
    }

    public void setNumberOfIronedClothes(int numberOfIronedClothes) {
        this.numberOfIronedClothes = numberOfIronedClothes;
    }
}
