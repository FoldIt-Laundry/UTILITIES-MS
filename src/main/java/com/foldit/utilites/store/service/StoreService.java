package com.foldit.utilites.store.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.store.dao.IStoreDetails;
import com.foldit.utilites.store.model.NearestStoreAvailableRequest;
import com.foldit.utilites.store.model.NearestStoreAvailableRespone;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import com.foldit.utilites.user.control.UserActionsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@Service
public class StoreService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(StoreService.class);

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private IStoreDetails iStoreDetails;

    public NearestStoreAvailableRespone getNearestAvailableStoreDetails(NearestStoreAvailableRequest nearestStoreAvailableRequest,String authToken) {
        try {
            validateAuthToken(authToken);
             iStoreDetails.getNearestAvailableStores(new Double[]{nearestStoreAvailableRequest.getxCordinates(), nearestStoreAvailableRequest.getyCordinates()});
            return null;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            // LOGGER.error("saveNewUserLocation(): Exception occured while saving the orderDetails: {} details to monogoDb, Exception: %s", toJson(orderDetails), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    private boolean validateAuthToken(String authToken) {
        if(!tokenVerificationService.validateAuthToken(authToken)) {
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        }
        return true;
    }



}
