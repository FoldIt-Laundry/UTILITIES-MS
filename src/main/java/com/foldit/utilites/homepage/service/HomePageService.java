package com.foldit.utilites.homepage.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.model.ServiceAvailable;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomePageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePageService.class);
    @Autowired
    private TokenValidationService tokenValidationService;
    @Autowired
    private ShopConfigurationHolder shopConfigurationHolder;

    public ServiceAvailable getAllAvailableService(String authToken, String userId, String mobileNumber) {
        try {
            tokenValidationService.authTokenValidationFromUserOrMobile(authToken, userId, mobileNumber);
            return new ServiceAvailable(true, shopConfigurationHolder.getAllAvailableServices());
        } catch (AuthTokenValidationException ex) {
            LOGGER.error("authTokenValidation(): Exception occurred while validating the auth token: {} from either userId: {} or mobileNumber: {}", authToken, userId, mobileNumber);
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getAllAvailableService(): Error while fetching the service details from mongodb db, Exception: {}", ex.getMessage());
            throw new MongoDBReadException(String.format("Failed to get data from mongoDb for getAllAvailableService() flow, Exception: %s", ex.getMessage()));
        }
    }

}
