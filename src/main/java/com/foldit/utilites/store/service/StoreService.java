package com.foldit.utilites.store.service;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.GoogleApiException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
import com.foldit.utilites.store.model.*;
import com.foldit.utilites.tokenverification.interfaces.TokenValidation;
import com.foldit.utilites.tokenverification.service.TokenValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.foldit.utilites.helper.GoogleMatrixForDeliveryFee.calculateDeliveryFee;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.store.dto.StoreDetailsConverter.getNearestStoreFromStoreDetails;

@Service
public class StoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreService.class);

    public StoreService(@Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup, @Autowired TokenValidationService tokenValidationService) {
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
        this.tokenValidation = tokenValidationService;
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IStoreDetails iStoreDetails;
    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;
    private TokenValidation tokenValidation;

    public AvailableStoreDetailsRespone getNearestAvailableStoreDetails(NearestStoreAvailableRequest nearestStoreAvailableRequest, String authToken) {
        AvailableStoreDetailsRespone availableStoreDetailsRespone = new AvailableStoreDetailsRespone();
        try {
            tokenValidation.authTokenValidationFromUserOrMobile(authToken, nearestStoreAvailableRequest.getUserId(), nearestStoreAvailableRequest.getMobileNumber());
            Point location = new Point(nearestStoreAvailableRequest.getLatitude(), nearestStoreAvailableRequest.getLongitude());
            Query query = new Query();
            query.addCriteria(Criteria.where("storeLocation.location")
                    .nearSphere(location)
                    .maxDistance(5));
            List<StoreDetails> storeDetailsList = mongoTemplate.find(query, StoreDetails.class, "StoreInformation");
            if (!CollectionUtils.isEmpty(storeDetailsList)) {
                StoreDetails storeDetails = storeDetailsList.get(0);
                availableStoreDetailsRespone = getNearestStoreFromStoreDetails(storeDetails);
            }
            return availableStoreDetailsRespone;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting the nearest store details: {} , Exception: %s", toJson(nearestStoreAvailableRequest), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public List<StoreDetails> getAllStoreDetails(String userId, String mobileNumber, String authToken) {
        try {
            tokenValidation.authTokenValidationFromUserOrMobile(authToken, userId, mobileNumber);
            return iStoreDetails.findAll();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting all store details for userId: {}, mobileNumber:{} and authToken: {} , Exception: %s", userId, mobileNumber, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public AvailableTimeSlotsForScheduledPickupResponse getTimeSlotsForScheduledPickup(AvailableTimeSlotsForScheduledPickupRequest availableTimeSlotsRequest, String authToken) {
        AvailableTimeSlotsForScheduledPickupResponse timeSlotsResponse = new AvailableTimeSlotsForScheduledPickupResponse();
        try {
            tokenValidation.authTokenValidationFromUserOrMobile(authToken, availableTimeSlotsRequest.getUserId(), availableTimeSlotsRequest.getMobileNumber());
            timeSlotsResponse.setAvailableSlots(iGetTimeSlotsForScheduledPickUp.getTimeSlotsForScheduledPickUp(availableTimeSlotsRequest.getShopOpeningTime(), availableTimeSlotsRequest.getShopClosingTime()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsForScheduledPickup(): Exception occured while getting time slots for request: {} , Exception: %s", toJson(availableTimeSlotsRequest), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
        return timeSlotsResponse;
    }

    public AvailableTimeSlotsForScheduledPickupResponse getTimeSlotsFromDefaultStoreTimings(String userId, String mobileNumber, String authToken) {
        AvailableTimeSlotsForScheduledPickupResponse timeSlotsResponse = new AvailableTimeSlotsForScheduledPickupResponse();
        try {
            tokenValidation.authTokenValidationFromUserOrMobile(authToken, userId, mobileNumber);
            StoreDetails storeDetails = iStoreDetails.getShopTimingsFromStoreId("66dcbe4b2f87e5390bc4177e");
            timeSlotsResponse.setAvailableSlots(iGetTimeSlotsForScheduledPickUp.getTimeSlotsForScheduledPickUp(storeDetails.getOperatingHourStartTime(), storeDetails.getOperatingHourEndTime()));
            return timeSlotsResponse;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsFromDefaultStoreTimings(): Exception occured while getting time slots FromDefaultStoreTimings, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public Double deliveryFeeCalculator(String authToken, DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest) {
        try {
            tokenValidation.authTokenValidationFromUserOrMobile(authToken, deliveryFeeCalculatorRequest.getUserId(), deliveryFeeCalculatorRequest.getMobileNumber());
            StoreDetails storeDetails = iStoreDetails.getShopTimingsFromStoreId("66dcbe4b2f87e5390bc4177e");
            return calculateDeliveryFee(deliveryFeeCalculatorRequest, storeDetails.getDeliveryFeePerKmAfterThreshold(), storeDetails.getFreeDeliveryDistanceAllowed());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }


}
