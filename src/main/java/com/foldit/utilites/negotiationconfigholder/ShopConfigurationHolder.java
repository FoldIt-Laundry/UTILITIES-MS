package com.foldit.utilites.negotiationconfigholder;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.homepage.model.Services;
import com.foldit.utilites.store.model.ServiceOffered;
import com.foldit.utilites.store.model.StoreDetails;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShopConfigurationHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopConfigurationHolder.class);
    private List<String> storeAdminIds;
    private List<String> storeRiderIds;
    private List<String> storeWorkerIds;
    private String shopOpeningTime;
    private String shopClosingTime;
    private List<ServiceOffered> allAvailableServices;
    private Map<String,Double> serviceIdVsServicePrice = new HashMap<>();

    @Autowired
    private IStoreDetails iStoreDetails;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;

    @PostConstruct
    public void populateConfigurations() {
        StoreDetails storeDetails = iStoreDetails.findById(negotiationConfigHolder.getDefaultShopId()).get();
        allAvailableServices = storeDetails.getServiceOffered();
        serviceIdVsServicePrice = allAvailableServices.stream().collect(Collectors.toMap(ServiceOffered::getServiceId, ServiceOffered::getPricing));
        storeAdminIds = storeDetails.getShopAdminIds();
        storeRiderIds = storeDetails.getShopRiderIds();
        storeWorkerIds = storeDetails.getShopWorkerIds();
        shopOpeningTime = storeDetails.getOperatingHourStartTime();
        shopClosingTime = storeDetails.getOperatingHourEndTime();
    }

    public void updateAllServicesInformation() {
        LOGGER.info("updateAllServicesInformation(): Request received to update all the services information");
        allAvailableServices = iStoreDetails.findById(negotiationConfigHolder.getDefaultShopId()).get().getServiceOffered();
        LOGGER.info("updateAllServicesInformation(): All the services information has been updated successfully");
    }

    public void updateServiceIdVsServicePricingMap(){
        LOGGER.info("updateServiceIdVsServicePricingMap(): Request received to update the serviceId vs service pricing map in db ");
        serviceIdVsServicePrice = allAvailableServices.stream().collect(Collectors.toMap(ServiceOffered::getServiceId, ServiceOffered::getPricing));
        LOGGER.info("updateServiceIdVsServicePricingMap(): serviceId vs service pricing map has been updated successfully in db ");
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

    public Map<String, Double> getServiceIdVsServicePrice() {
        return serviceIdVsServicePrice;
    }
    public String getShopOpeningTime() {
        return shopOpeningTime;
    }

    public String getShopClosingTime() {
        return shopClosingTime;
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

    public List<ServiceOffered> getAllAvailableServices() {
        return allAvailableServices;
    }
}
