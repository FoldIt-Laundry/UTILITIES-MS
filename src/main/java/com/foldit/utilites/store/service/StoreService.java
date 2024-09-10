package com.foldit.utilites.store.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.store.dao.IStoreDetails;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfaces.TokenValidation;
import com.foldit.utilites.store.interfacesimp.RedisAuthTokenValidation;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
import com.foldit.utilites.store.model.*;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import com.foldit.utilites.user.control.UserActionsController;
import com.mongodb.client.model.geojson.Position;
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
import java.util.Objects;

import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.store.dto.StoreDetailsConverter.getNearestStoreFromStoreDetails;

@Service
public class StoreService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(StoreService.class);

    public StoreService(@Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup, @Autowired RedisAuthTokenValidation redisAuthTokenValidation) {
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
        this.tokenValidation = redisAuthTokenValidation;
    }

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IStoreDetails iStoreDetails;
    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;
    private TokenValidation tokenValidation;

    public NearestStoreAvailableRespone getNearestAvailableStoreDetails(NearestStoreAvailableRequest nearestStoreAvailableRequest,String authToken) {
        NearestStoreAvailableRespone nearestStoreAvailableRespone = new NearestStoreAvailableRespone();
        try {
            tokenValidation.authTokenValidation(authToken, nearestStoreAvailableRequest.getUserId(), nearestStoreAvailableRequest.getMobileNumber());
            Point location = new Point(nearestStoreAvailableRequest.getLatitude(), nearestStoreAvailableRequest.getLongitude());
            Query query = new Query();
            query.addCriteria(Criteria.where("storeLocation.location")
                    .nearSphere(location)
                    .maxDistance(5));
            List<StoreDetails> storeDetailsList = mongoTemplate.find(query, StoreDetails.class, "StoreInformation");
            if(!CollectionUtils.isEmpty(storeDetailsList)) {
                StoreDetails storeDetails = storeDetailsList.get(0);
                nearestStoreAvailableRespone = getNearestStoreFromStoreDetails(storeDetails);
            }
            return nearestStoreAvailableRespone;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting the nearest store details: {} , Exception: %s", toJson(nearestStoreAvailableRequest), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public AvailableTimeSlotsForScheduledPickupResponse getTimeSlotsForScheduledPickup(AvailableTimeSlotsForScheduledPickupRequest availableTimeSlotsRequest, String authToken) {
        AvailableTimeSlotsForScheduledPickupResponse timeSlotsResponse = new AvailableTimeSlotsForScheduledPickupResponse();
        try {
            tokenValidation.authTokenValidation(authToken, availableTimeSlotsRequest.getUserId(), availableTimeSlotsRequest.getMobileNumber());
            timeSlotsResponse.setAvailableSlots(iGetTimeSlotsForScheduledPickUp.getTimeSlotsForScheduledPickUp(availableTimeSlotsRequest.getShopOpeningTime(), availableTimeSlotsRequest.getShopClosingTime()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsForScheduledPickup(): Exception occured while getting time slots for request: {} , Exception: %s", toJson(availableTimeSlotsRequest), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
        return timeSlotsResponse;
    }





}
