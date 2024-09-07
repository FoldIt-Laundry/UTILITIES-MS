package com.foldit.utilites.store.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.store.model.NearestStoreAvailableRequest;
import com.foldit.utilites.store.model.NearestStoreAvailableRespone;
import com.foldit.utilites.store.service.StoreService;
import com.foldit.utilites.user.control.UserActionsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class StoreController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsController.class);

    @Autowired
    private StoreService storeService;

    @GetMapping("store/getNearestAvailableStoreDetails")
    public ResponseEntity<NearestStoreAvailableRespone> getNearestAvailableStoreDetails(@RequestBody NearestStoreAvailableRequest nearestStoreAvailableRequest, @RequestHeader(value="authToken") String authToken) {
        NearestStoreAvailableRespone nearestStoreAvailableRespone ;
        try {
            LOGGER.info("getNearestAvailableStoreDetails(): Initiating request to fetch the nearest available store details from given request: {} and authToken: {}", toJson(nearestStoreAvailableRequest), authToken);
            nearestStoreAvailableRespone = storeService.getNearestAvailableStoreDetails(nearestStoreAvailableRequest, authToken);
            return new ResponseEntity<>(nearestStoreAvailableRespone, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getNearestAvailableStoreDetails(): Exception occured while getting the nearest available servicable store details, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}
