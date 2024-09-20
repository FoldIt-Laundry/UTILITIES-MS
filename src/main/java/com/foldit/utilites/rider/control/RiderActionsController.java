package com.foldit.utilites.rider.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.service.RiderActionsService;
import com.foldit.utilites.worker.controller.WorkerActionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RiderActionsController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(RiderActionsController.class);

    @Autowired
    private RiderActionsService riderActionsService;

    @GetMapping("/rider/getNextPickUpOrderDetails")
    public ResponseEntity<List<OrderDetails>> getNextPickUpOrderDetails(@RequestHeader(value="authToken") String authToken, @RequestParam("riderId") String riderId) {
        List<OrderDetails> orderDetails;
        try {
            LOGGER.info("getNextPickUpOrderDetails(): Get all next pickup order details for riderId: {} and authToken: {}", riderId, authToken);
            orderDetails = riderActionsService(authToken, riderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }


}
