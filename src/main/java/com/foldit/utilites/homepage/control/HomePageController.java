package com.foldit.utilites.homepage.control;

import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.model.ServiceAvailable;
import com.foldit.utilites.homepage.service.HomePageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HomePageController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(HomePageController.class);

    @Autowired
    private HomePageService homePageService;

    @GetMapping("/homepage/serviceAvailable")
    public ResponseEntity<ServiceAvailable> getListOfAvailableServices(@RequestHeader(value="authToken") String authToken, @RequestParam(required = false) String userId,@RequestParam(required = false) String mobileNumber) {
        ServiceAvailable serviceAvailable = new ServiceAvailable();
        try {
            LOGGER.info("getListOfAvailableServices(): Initiating request to get the list of available services for authToken: {} and userId: {} or mobileNumber: {}", authToken, userId, mobileNumber);
            serviceAvailable= homePageService.getAllAvailableService(authToken, userId, mobileNumber);
            return new ResponseEntity<>(serviceAvailable, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return new ResponseEntity<>(serviceAvailable, HttpStatus.OK);
        }
    }

    @GetMapping("/test")
    public String get(){
        return "Application up and running";
    }

}
