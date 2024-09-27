package com.foldit.utilites.user.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.user.model.*;
import com.foldit.utilites.user.service.UserActionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class UserActionsController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsController.class);

    @Autowired
    private UserActionsService userActionsService;

    @PatchMapping("userActions/saveNewLocation")
    public ResponseEntity<OnBoardNewUserLocation> saveNewUserLocation(@RequestBody OnBoardNewUserLocation userLocation, @RequestHeader(value = "authToken") String authToken) {
        OnBoardNewUserLocation onBoardingNewUserLocation;
        try{
            LOGGER.info("saveNewUserLocation(): Request received to store the new user location: {} and auth-Token: {}", toJson(userLocation), authToken);
            onBoardingNewUserLocation = userActionsService.saveNewUserLocation(userLocation, authToken);
            return new ResponseEntity<>(onBoardingNewUserLocation, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(userLocation, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("userActions/getUserDetailsFromUserId")
    public ResponseEntity<UserDetails> getUserDetailsFromMobileNumber(@RequestHeader(value = "authToken") String authToken, @RequestParam String userId) {
        UserDetails userDetailsFromDb;
        try {
            LOGGER.info("getUserDetailsFromMobileNumber(): Request received to get the user details from userId: {} and auth-Token: {}", userId, authToken);
            userDetailsFromDb = userActionsService.getUserDetailsFromUserId(authToken, userId);
            return new ResponseEntity<>(userDetailsFromDb, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getUserDetailsFromMobileNumber(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(new UserDetails(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("userActions/getAllUserLocations")
    public ResponseEntity<List<UserLocation>> getAllUserLocations(@RequestHeader(value = "authToken") String authToken, @RequestParam String userId) {
        List<UserLocation> userLocations;
        try {
            LOGGER.info("getAllUserLocations(): Request received to get the all the user locations from userId: {} and auth-Token: {}", userId, authToken);
            userLocations = userActionsService.getAllUserLocations(authToken, userId);
            return new ResponseEntity<>(userLocations, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getAllUserLocations(): Exception occured while getting all the user location, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/userActions/cancelOnGoingOrder")
    public ResponseEntity<Boolean> cancelOnGoingOrder(@RequestHeader(value="authToken") String authToken, @RequestBody CancelOrderRequest cancelOrderRequest) {
        try {
            LOGGER.info("cancelOnGoingOrder(): Initiating request to cancel the order details: {} and authToken: {}", toJson(cancelOrderRequest), authToken);
            userActionsService.cancelOnGoingOrder(authToken, cancelOrderRequest);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            LOGGER.error("cancelOnGoingOrder(): Request data validation failed for the input request object: {}, Exception: {}", toJson(cancelOrderRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        } catch (AuthTokenValidationException ex) {
            LOGGER.error("cancelOnGoingOrder(): Auth Validation failed for userId: {} and authToken: {}, Exception: {}", cancelOrderRequest.userId(), authToken, ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("cancelOnGoingOrder(): Exception occurred while processing the details for payload: {}, Exception: {}", toJson(cancelOrderRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping("/userActions/rescheduleOnGoingOrder")
    public ResponseEntity<Boolean> rescheduleOnGoingOrder(@RequestHeader(value="authToken") String authToken, @RequestBody RescheduleOrderRequest rescheduleOrderRequest) {
        try {
            LOGGER.info("rescheduleOnGoingOrder(): Initiating request to reschedule on going order the order details: {} and authToken: {}", toJson(rescheduleOrderRequest), authToken);
            userActionsService.rescheduleOnGoingOrder(authToken, rescheduleOrderRequest);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            LOGGER.error("rescheduleOnGoingOrder(): Request data validation failed for the input request object: {}, Exception: {}", toJson(rescheduleOrderRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        } catch (AuthTokenValidationException ex) {
            LOGGER.error("rescheduleOnGoingOrder(): Auth Validation failed for userId: {} and authToken: {}, Exception: {}", rescheduleOrderRequest.userId(), authToken, ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("rescheduleOnGoingOrder(): Exception occurred while processing the details for payload: {}, Exception: {}", toJson(rescheduleOrderRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

}
