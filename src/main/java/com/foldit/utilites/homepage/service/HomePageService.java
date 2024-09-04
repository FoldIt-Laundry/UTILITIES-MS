package com.foldit.utilites.homepage.service;

import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.control.HomePageController;
import com.foldit.utilites.homepage.dao.IServiceOffered;
import com.foldit.utilites.homepage.model.ServiceAvailable;
import com.foldit.utilites.homepage.model.Services;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomePageService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(HomePageService.class);
    @Autowired
    private IServiceOffered iServiceOffered;

    @Autowired
    private TokenVerificationService tokenVerificationService;

    public ServiceAvailable getAllAvailableService(String authToken) {
        List<Services> servicesList;
        try {
            if(!tokenVerificationService.validateAuthToken(authToken)) {
                return new ServiceAvailable();
            }
            servicesList = iServiceOffered.findAll();
            return new ServiceAvailable(true, servicesList);
        } catch (Exception ex) {
            LOGGER.error("getAllAvailableService(): Error while fetching the service details from mongodb db, Exception: {}", ex.getMessage());
            throw new MongoDBReadException(String.format("Failed to get data from mongoDb for getAllAvailableService() flow, Exception: %s", ex.getMessage()));
        }
    }


}
