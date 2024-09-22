package com.foldit.utilites.user.service;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.GoogleApiException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.store.model.DeliveryFeeCalculatorRequest;
import com.foldit.utilites.store.model.StoreDetails;
import com.foldit.utilites.tokenverification.service.RedisTokenVerificationService;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.user.model.DeliveryAndFeeDetails;
import com.foldit.utilites.user.model.OnBoardNewUserLocation;
import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.user.model.UserLocation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.foldit.utilites.helper.GoogleMatrixForDeliveryFee.calculateDeliveryFee;
import static com.foldit.utilites.helper.GoogleMatrixForDeliveryFee.getDeliveryFeeAndDistanceDetails;

@Service
public class UserActionsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsService.class);

    @Autowired
    private RedisTokenVerificationService redisTokenVerificationService;

    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private IStoreDetails iStoreDetails;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    public OnBoardNewUserLocation saveNewUserLocation(OnBoardNewUserLocation onBoardNewUserLocation, String authToken) {
        UserLocation userLocation;
        try {
            if(!redisTokenVerificationService.validateAuthToken(onBoardNewUserLocation.getUserId(), authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            userLocation = onBoardNewUserLocation.getUserLocation();

            DeliveryAndFeeDetails deliveryAndFeeDetails = deliveryFeeCalculatorFromDefaultStoreAddress(userLocation);
            userLocation.setDistanceFromNearestStore(deliveryAndFeeDetails.getDistanceFromNearestStore());
            userLocation.setDeliveryFeeIfApplicable(deliveryAndFeeDetails.getDeliveryFee());

            Query query = new Query(Criteria.where("id").is(onBoardNewUserLocation.getUserId()));
            Update update = new Update().addToSet("locations", onBoardNewUserLocation.getUserLocation());
            if(StringUtils.isNotBlank(onBoardNewUserLocation.getUserName())) {
                update.set("userName", onBoardNewUserLocation.getUserName());
            }
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
            if(!redisTokenVerificationService.validateAuthToken(userId, authToken)) {
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

    @Transactional(readOnly = true)
    public List<UserLocation> getAllUserLocations(String authToken, String userId) {
        try {
            if(!redisTokenVerificationService.validateAuthToken(userId, authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            return Optional.ofNullable(iUserDetails.getAllUserLocationFromUserId(userId)).orElseGet(() -> new UserDetails(new ArrayList<>())).getLocations();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the user details from monogoDb, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    public DeliveryAndFeeDetails deliveryFeeCalculatorFromDefaultStoreAddress(UserLocation userLocation) {
        try {
            StoreDetails storeDetails = iStoreDetails.getShopDeliveryFeeRelatedInformation("66dcbe4b2f87e5390bc4177e");
            DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest = new DeliveryFeeCalculatorRequest();
            deliveryFeeCalculatorRequest.setSourceLatitude(String.valueOf((userLocation.getLatitude())));
            deliveryFeeCalculatorRequest.setGoogleApiKey(negotiationConfigHolder.getGoogleApiKeyForDistanceMatrix());
            deliveryFeeCalculatorRequest.setSourceLongitude(String.valueOf(userLocation.getLongitude()));
            deliveryFeeCalculatorRequest.setDestinationLatitude(String.valueOf(storeDetails.getStoreLocation().getLocation().getCoordinates().get(0)));
            deliveryFeeCalculatorRequest.setDestinationLongitude(String.valueOf(storeDetails.getStoreLocation().getLocation().getCoordinates().get(1)));
            return getDeliveryFeeAndDistanceDetails(deliveryFeeCalculatorRequest, storeDetails.getDeliveryFeePerKmAfterThreshold(), storeDetails.getFreeDeliveryDistanceAllowed());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }

}
