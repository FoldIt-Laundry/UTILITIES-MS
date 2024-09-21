package com.foldit.utilites.negotiationconfigholder;

import com.foldit.utilites.dao.IServiceOffered;
import com.foldit.utilites.homepage.model.Services;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ServiceNegotiationConfigHolder {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ServiceNegotiationConfigHolder.class);
    private Map<String,Double> serviceIdVsServicePrice = new HashMap<>();

    @Autowired
    private IServiceOffered iServiceOffered;

    @PostConstruct
    public void populateConfigurations() {
        serviceIdVsServicePrice = iServiceOffered.findAll().stream().collect(Collectors.toMap(Services::getServiceId, Services::getPricing));
    }

    public void updateServiceIdVsServicePricingMap(){
        LOGGER.info("updateServiceIdVsServicePricingMap(): Request received to update the serviceId vs service pricing map in db ");
        serviceIdVsServicePrice = iServiceOffered.findAll().stream().collect(Collectors.toMap(Services::getServiceId, Services::getPricing));
        LOGGER.info("updateServiceIdVsServicePricingMap(): serviceId vs service pricing map has been updated successfully in db ");
    }

    public Map<String, Double> getServiceIdVsServicePrice() {
        return serviceIdVsServicePrice;
    }
}
