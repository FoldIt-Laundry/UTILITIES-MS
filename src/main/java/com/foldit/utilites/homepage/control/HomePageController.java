package com.foldit.utilites.homepage.control;

import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.homepage.model.ServiceAvailable;
import com.foldit.utilites.homepage.service.HomePageService;
import com.foldit.utilites.image.control.ImageOperationsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomePageController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(HomePageController.class);

    @Autowired
    private HomePageService homePageService;

    @GetMapping("/homepage/serviceAvailable")
    public ResponseEntity<ServiceAvailable> getListOfAvailableServices(@RequestHeader(value="authToken") String authToken, @RequestParam String userId) {
        ServiceAvailable serviceAvailable;
        try {
            LOGGER.info("getListOfAvailableServices(): Initiating request to get the list of available services for authToken: {} and userId: {}", authToken, userId);
            serviceAvailable= homePageService.getAllAvailableService(authToken, userId);
            return new ResponseEntity<>(serviceAvailable, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


}
