package com.foldit.utilites.store.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;


public class NearestStoreAvailableRespone {
    private String storeId;
    private String shopName;
    private String shopAddress;
    private String operatingHourStartTime;
    private String operatingHourEndTime;
    private List<ServiceOffered> serviceOffered;

    public NearestStoreAvailableRespone() {
    }

    public List<ServiceOffered> getServiceOffered() {
        return serviceOffered;
    }

    public void setServiceOffered(List<ServiceOffered> serviceOffered) {
        this.serviceOffered = serviceOffered;
    }

    public String getOperatingHourStartTime() {
        return operatingHourStartTime;
    }

    public void setOperatingHourStartTime(String operatingHourStartTime) {
        this.operatingHourStartTime = operatingHourStartTime;
    }

    public String getOperatingHourEndTime() {
        return operatingHourEndTime;
    }

    public void setOperatingHourEndTime(String operatingHourEndTime) {
        this.operatingHourEndTime = operatingHourEndTime;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }
}
