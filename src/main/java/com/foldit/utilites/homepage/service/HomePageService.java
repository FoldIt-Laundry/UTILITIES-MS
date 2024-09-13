package com.foldit.utilites.homepage.service;

import com.foldit.utilites.dao.IServiceOffered;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.model.ServiceAvailable;
import com.foldit.utilites.homepage.model.Services;
import com.foldit.utilites.tokenverification.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomePageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePageService.class);
    @Autowired
    private IServiceOffered iServiceOffered;
    @Autowired
    private TokenValidationService tokenValidationService;

    public ServiceAvailable getAllAvailableService(String authToken, String userId, String mobileNumber) {
        List<Services> servicesList;
        try {
            tokenValidationService.authTokenValidationFromUserOrMobile(authToken, userId, mobileNumber);
            servicesList = iServiceOffered.findAll();

            double lat1 = 12.9210488; // Source latitude
            double lon1 = 77.6781733; // Source longitude
            double lat2 = 13.0621163; // Destination latitude
            double lon2 = 77.6604752;  // Destination longitude


            return new ServiceAvailable(true, servicesList);
        } catch (Exception ex) {
            LOGGER.error("getAllAvailableService(): Error while fetching the service details from mongodb db, Exception: {}", ex.getMessage());
            throw new MongoDBReadException(String.format("Failed to get data from mongoDb for getAllAvailableService() flow, Exception: %s", ex.getMessage()));
        }
    }

}
