package com.foldit.utilites.store.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.GoogleApiException;
import com.foldit.utilites.store.model.*;
import com.foldit.utilites.store.service.StoreService;
import com.foldit.utilites.user.control.UserActionsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class StoreController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsController.class);

    @Autowired
    private StoreService storeService;

    @PostMapping("store/getNearestAvailableStoreDetails")
    public ResponseEntity<AvailableStoreDetailsRespone> getNearestAvailableStoreDetails(@RequestBody NearestStoreAvailableRequest nearestStoreAvailableRequest, @RequestHeader(value = "authToken") String authToken) {
        AvailableStoreDetailsRespone availableStoreDetailsRespone;
        try {
            LOGGER.info("getNearestAvailableStoreDetails(): Initiating request to fetch the nearest available store details from given request: {} and authToken: {}", toJson(nearestStoreAvailableRequest), authToken);
            availableStoreDetailsRespone = storeService.getNearestAvailableStoreDetails(nearestStoreAvailableRequest, authToken);
            return new ResponseEntity<>(availableStoreDetailsRespone, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting the nearest available servicable store details, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("store/getAllStoreDetails")
    public ResponseEntity<List<StoreDetails>> getAllStoreDetails(@RequestParam(required = false) String userId, @RequestParam(required = false) String mobileNumber, @RequestHeader(value = "authToken") String authToken) {
        List<StoreDetails> storeDetails;
        try {
            LOGGER.info("getAllStoreDetails(): Initiating request to fetch the all available store details from given userId: {} or mobileNumber: {} and authToken: {}", userId, mobileNumber, authToken);
            storeDetails = storeService.getAllStoreDetails(userId, mobileNumber, authToken);
            return new ResponseEntity<>(storeDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting all the stores available details, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("store/getTimeSlotsForScheduledPickup")
    public ResponseEntity<AvailableTimeSlotsForScheduledPickupResponse> getTimeSlotsForScheduledPickup(@RequestBody AvailableTimeSlotsForScheduledPickupRequest availableTimeSlotsForScheduledPickupRequest, @RequestHeader(value = "authToken") String authToken) {
        AvailableTimeSlotsForScheduledPickupResponse timeSlots;
        try {
            LOGGER.info("getTimeSlotsForScheduledPickup(): Initiating request to fetch the next 20 available time slots from given request: {} and authToken: {}", toJson(availableTimeSlotsForScheduledPickupRequest), authToken);
            timeSlots = storeService.getTimeSlotsForScheduledPickup(availableTimeSlotsForScheduledPickupRequest, authToken);
            return new ResponseEntity<>(timeSlots, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsForScheduledPickup(): Exception occurred while getting the time slots for scheduled pickup for payload: {}, Exception: {}", toJson(availableTimeSlotsForScheduledPickupRequest), ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("store/getTimeSlotsFromDefaultStoreTimings")
    public ResponseEntity<AvailableTimeSlotsForScheduledPickupResponse> getTimeSlotsFromDefaultStoreTimings(@RequestParam(required = false) String userId, @RequestParam(required = false) String mobileNumber, @RequestHeader(value = "authToken") String authToken) {
        AvailableTimeSlotsForScheduledPickupResponse timeSlots;
        try {
            LOGGER.info("getTimeSlotsFromCurrentTime(): Initiating request to fetch the next 20 available time slots ");
            timeSlots = storeService.getTimeSlotsFromDefaultStoreTimings(userId, mobileNumber, authToken);
            return new ResponseEntity<>(timeSlots, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getTimeSlotsFromDefaultStoreTimings(): Exception occurred while getting the time slots FromDefaultStoreTimings, Exception: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/store/deliveryFeeCalculatorFromDefaultStore")
    public ResponseEntity<Double> deliveryFeeCalculator(@RequestHeader(value = "authToken") String authToken, @RequestBody DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest) {
        Double deliveryFee;
        try {
            LOGGER.info("deliveryFeeCalculator(): Initiating request to get the delivery free for request: {} and authToken: {} ", toJson(deliveryFeeCalculatorRequest), authToken);
            deliveryFee = storeService.deliveryFeeCalculator(authToken, deliveryFeeCalculatorRequest);
            return new ResponseEntity<>(deliveryFee, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }

}
