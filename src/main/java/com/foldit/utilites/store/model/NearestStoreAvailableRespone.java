package com.foldit.utilites.store.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


public class NearestStoreAvailableRespone {
    private String storeId;
    private String shopName;
    private String shopAddress;

    public NearestStoreAvailableRespone() {
    }

    public NearestStoreAvailableRespone(String storeId, String shopName, String shopAddress) {
        this.storeId = storeId;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
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
