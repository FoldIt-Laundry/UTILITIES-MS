package com.foldit.utilites.negotiationconfigholder;

import com.foldit.utilites.dao.IConfigDetails;
import com.foldit.utilites.homepage.model.Services;
import com.foldit.utilites.dao.IServiceOffered;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NegotiationConfigHolder {

    private static final Logger LOGGER =  LoggerFactory.getLogger(NegotiationConfigHolder.class);

    private String googleApiKeyForDistanceMatrix;
    private String defaultShopId;

    @Autowired
    private IConfigDetails iConfigDetails;


    @PostConstruct
    public void populateConfigurations() {
        googleApiKeyForDistanceMatrix = populateNegotiationConfig("GOOGLE_API_KEY_FOR_DISTANCE_MATRIX");
        defaultShopId = populateNegotiationConfig("DEFAULT_STORE_ID");
    }

    public String populateNegotiationConfig(String key) {
        return iConfigDetails.getConfigValue(key).getConfigValue();
    }

    public String getGoogleApiKeyForDistanceMatrix() {
        return googleApiKeyForDistanceMatrix;
    }

    public String getDefaultShopId() {
        return defaultShopId;
    }
}
