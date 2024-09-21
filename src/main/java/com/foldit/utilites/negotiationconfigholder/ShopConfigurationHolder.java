package com.foldit.utilites.negotiationconfigholder;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.store.model.StoreDetails;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopConfigurationHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopConfigurationHolder.class);
    private List<String> storeAdminIds;
    private List<String> storeRiderIds;
    private List<String> storeWorkerIds;

    @Autowired
    private IStoreDetails iStoreDetails;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;

    @PostConstruct
    public void populateConfigurations() {
        StoreDetails storeDetails = iStoreDetails.getShopAdminWorkerRiderIds(negotiationConfigHolder.getDefaultShopId());
        storeAdminIds = storeDetails.getShopAdminIds();
        storeRiderIds = storeDetails.getShopRiderIds();
        storeWorkerIds = storeDetails.getShopWorkerIds();
    }

    public void updateRiderIds() {
        String shopId = negotiationConfigHolder.getDefaultShopId();
        LOGGER.info("updateRiderIds(): Request received to update the riders id for the shopId: {}", shopId);
        storeRiderIds = iStoreDetails.getShopRiderIds(shopId).getShopRiderIds();
        LOGGER.info("updateRiderIds(): Riders Id has been updated successfully in db");
    }

    public void updateAdminIds() {
        String shopId = negotiationConfigHolder.getDefaultShopId();
        LOGGER.info("updateAdminIds(): Request received to update the admin id for the shopId: {}", shopId);
        storeRiderIds = iStoreDetails.getShopAdminIds(shopId).getShopAdminIds();
        LOGGER.info("updateAdminIds(): Riders Id has been updated successfully in db");
    }

    public void updateWorkerIds() {
        String shopId = negotiationConfigHolder.getDefaultShopId();
        LOGGER.info("updateWorkerIds(): Request received to update the worker id for the shopId: {}", shopId);
        storeRiderIds = iStoreDetails.getShopWorkerIds(shopId).getShopWorkerIds();
        LOGGER.info("updateWorkerIds(): Riders Id has been updated successfully in db");
    }

    public List<String> getStoreAdminIds() {
        return storeAdminIds;
    }

    public List<String> getStoreRiderIds() {
        return storeRiderIds;
    }

    public List<String> getStoreWorkerIds() {
        return storeWorkerIds;
    }
}
