package com.foldit.utilites.rider.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.model.MarkOrderOutForDeliveryRequest;
import com.foldit.utilites.rider.model.MarkOrderOutForDeliveryResponse;
import com.foldit.utilites.rider.model.OrderDeliveredRequest;
import com.foldit.utilites.rider.model.OrderDeliveredResponse;
import com.foldit.utilites.rider.service.RiderActionsService;
import com.foldit.utilites.worker.controller.WorkerActionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

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


    @PostMapping("/rider/markOrderOutForDelivery")
    public ResponseEntity<MarkOrderOutForDeliveryResponse> markOrderOutForDelivery(@RequestHeader(value="authToken") String authToken, @RequestBody MarkOrderOutForDeliveryRequest markOrderOutForDeliveryRequest) {
        try {
            LOGGER.info("markOrderOutForDelivery(): Initiating request to mark the order out for delivery for request details: {} and authToken: {}", toJson(markOrderOutForDeliveryRequest), authToken);
            riderActionsService.markOrderOutForDelivery(authToken, markOrderOutForDeliveryRequest);
            return new ResponseEntity<>(new MarkOrderOutForDeliveryResponse(true), HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

    @PostMapping("/rider/confirmOrderDelivery")
    public ResponseEntity<OrderDeliveredResponse> confirmOrderDelivery(@RequestHeader(value="authToken") String authToken, @RequestBody OrderDeliveredRequest orderDeliveredRequest) {
        try {
            LOGGER.info("confirmOrderDelivery(): Initiating request to complete the order for request details: {} and authToken: {}", toJson(orderDeliveredRequest), authToken);
            riderActionsService.confirmOrderDelivery(authToken, orderDeliveredRequest);
            return new ResponseEntity<>(new OrderDeliveredResponse(true), HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }


}
