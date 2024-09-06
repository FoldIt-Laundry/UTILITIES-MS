package com.foldit.utilites.user.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import com.foldit.utilites.user.dao.IUserDetails;
import com.foldit.utilites.user.model.OnBoardNewUserLocation;
import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.user.model.UserLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserActionsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsService.class);

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private IUserDetails iUserDetails;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public OnBoardNewUserLocation saveNewUserLocation(OnBoardNewUserLocation onBoardNewUserLocation, String authToken) {
        try {
            if(!tokenVerificationService.validateAuthToken(authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            Query query = new Query(Criteria.where("id").is(onBoardNewUserLocation.getUserId()));
            Update update = new Update().addToSet("locations", onBoardNewUserLocation.getUserLocation());
            mongoTemplate.updateFirst(query, update, UserLocation.class);
            return onBoardNewUserLocation;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UserDetails getUserDetailsFromUserId(String authToken, String userId) {
        try {
            if(!tokenVerificationService.validateAuthToken(authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            return iUserDetails.findById(userId).orElseGet(() -> (new UserDetails()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the user details from monogoDb, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
