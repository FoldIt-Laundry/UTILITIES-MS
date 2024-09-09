package com.foldit.utilites.user.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.control.HomePageController;
import com.foldit.utilites.user.model.OnBoardNewUserLocation;
import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.user.model.UserLocation;
import com.foldit.utilites.user.service.UserActionsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class UserActionsController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsController.class);

    @Autowired
    private UserActionsService userActionsService;

    @PatchMapping("userActions/saveNewLocation")
    public ResponseEntity<OnBoardNewUserLocation> saveNewUserLocation(@RequestBody OnBoardNewUserLocation userLocation, @RequestParam String authToken) {
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
    public ResponseEntity<UserDetails> getUserDetailsFromMobileNumber(@RequestParam String authToken, @RequestParam String userId) {
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
    public ResponseEntity<List<UserLocation>> getAllUserLocations(@RequestParam String authToken, @RequestParam String userId) {
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

}
