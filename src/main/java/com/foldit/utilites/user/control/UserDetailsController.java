package com.foldit.utilites.user.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.user.model.OnBoardNewUserLocation;
import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.user.model.UserLocation;
import com.foldit.utilites.user.service.UserActionsService;
import com.foldit.utilites.user.service.UserDetailsService;
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
public class UserDetailsController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserDetailsController.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @PatchMapping("userDetails/saveFcmToken")
    public ResponseEntity<Boolean> saveFcmToken(@RequestParam String userId, @RequestParam String fcmToken, @RequestHeader(value = "authToken") String authToken) {
        try{
            LOGGER.info("saveFcmToken(): Request received to store the fcm token for userId: {}, fcmToken: {} and auth-Token: {}", userId, fcmToken, authToken);
            userDetailsService.saveFcmToken(authToken, userId, fcmToken);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveFcmToken(): Exception occurred while saving the fcm token, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }




}
