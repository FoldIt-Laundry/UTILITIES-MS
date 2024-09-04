package com.foldit.utilites.order.model;

public class Services {
    private String serviceName;
    private String serviceId;
    private Double clothesKgSize;
    private Integer numberOfIronedClothes;

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

    public Double getClothesKgSize() {
        return clothesKgSize;
    }

    public void setClothesKgSize(Double clothesKgSize) {
        this.clothesKgSize = clothesKgSize;
    }

    public Integer getNumberOfIronedClothes() {
        return numberOfIronedClothes;
    }

    public void setNumberOfIronedClothes(Integer numberOfIronedClothes) {
        this.numberOfIronedClothes = numberOfIronedClothes;
    }
}
