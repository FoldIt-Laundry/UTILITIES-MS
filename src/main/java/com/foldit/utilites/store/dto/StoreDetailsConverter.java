package com.foldit.utilites.store.dto;

import com.foldit.utilites.store.model.NearestStoreAvailableRespone;
import com.foldit.utilites.store.model.StoreDetails;

public class StoreDetailsConverter {

    public static NearestStoreAvailableRespone getNearestStoreFromStoreDetails(StoreDetails storeDetails) {
        NearestStoreAvailableRespone nearestStoreAvailableRespone = new NearestStoreAvailableRespone();
        nearestStoreAvailableRespone.setStoreId(storeDetails.getId());
        nearestStoreAvailableRespone.setShopAddress(storeDetails.getStoreLocation().getAddressLine1());
        nearestStoreAvailableRespone.setShopName(storeDetails.getName());
        nearestStoreAvailableRespone.setOperatingHourEndTime(storeDetails.getOperatingHourEndTime());
        nearestStoreAvailableRespone.setOperatingHourStartTime(storeDetails.getOperatingHourStartTime());
        nearestStoreAvailableRespone.setServiceOffered(storeDetails.getServiceOffered());
        return nearestStoreAvailableRespone;
    }

}
