package com.foldit.utilites.store.dto;

import com.foldit.utilites.store.model.AvailableStoreDetailsRespone;
import com.foldit.utilites.store.model.StoreDetails;

public class StoreDetailsConverter {

    public static AvailableStoreDetailsRespone getNearestStoreFromStoreDetails(StoreDetails storeDetails) {
        AvailableStoreDetailsRespone availableStoreDetailsRespone = new AvailableStoreDetailsRespone();
        availableStoreDetailsRespone.setStoreId(storeDetails.getId());
        availableStoreDetailsRespone.setShopAddress(storeDetails.getStoreLocation().getAddressLine1());
        availableStoreDetailsRespone.setShopName(storeDetails.getName());
        availableStoreDetailsRespone.setOperatingHourEndTime(storeDetails.getOperatingHourEndTime());
        availableStoreDetailsRespone.setOperatingHourStartTime(storeDetails.getOperatingHourStartTime());
        availableStoreDetailsRespone.setServiceOffered(storeDetails.getServiceOffered());
        return availableStoreDetailsRespone;
    }

}
