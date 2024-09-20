package com.foldit.utilites.worker.controller;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.worker.model.*;
import com.foldit.utilites.worker.service.WorkerActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class WorkerActionController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(WorkerActionController.class);

    @Autowired
    private WorkerActionService workerActionService;

    @GetMapping("/worker/getAllUnApprovedOrderList")
    public ResponseEntity<List<OrderDetails>> getAllUnApprovedOrderList(@RequestHeader(value="authToken") String authToken, @RequestParam("workerId") String workerId) {
        List<OrderDetails> orderDetails;
        try {
            LOGGER.info("getAllUnApprovedOrderList(): Request received to get all the unapproved orders list for workerId: {} and authToken: {}", workerId, authToken);
            orderDetails = workerActionService.getAllUnApprovedOrderList(workerId, authToken);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

    @PostMapping("/worker/approvePendingOrder")
    public ResponseEntity<ApproveOrderResponse> approvePendingOrder(@RequestHeader(value="authToken") String authToken, @RequestBody ApproveOrderRequest approveOrderRequest) {
        try {
            LOGGER.info("approvePendingOrder(): Request received to approve the pending order details :{} and authToken: {}", toJson(approveOrderRequest), authToken);
            workerActionService.approvePendingOrder(authToken, approveOrderRequest);
            return new ResponseEntity<>(new ApproveOrderResponse(true), HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

    @PostMapping("/worker/markWorkInProgress")
    public ResponseEntity<MarkWorkInProgressResponse> markWorkInProgress(@RequestHeader(value="authToken") String authToken, @RequestBody MarkWorkInProgressRequest markWorkInProgressRequest) {
        try {
            LOGGER.info("markWorkInProgress(): Request received to mark the work in progress and order has reached the shop for payload:{} and authToken: {}", toJson(markWorkInProgressRequest), authToken);
            workerActionService.markWorkInProgress(authToken, markWorkInProgressRequest);
            return new ResponseEntity<>(new MarkWorkInProgressResponse(true), HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }


    @PostMapping("/worker/markOrderReadyForDelivery ")
    public ResponseEntity<MarkOrderReadyForDeliveryResponse> markOrderReadyForDelivery(@RequestHeader(value="authToken") String authToken, @RequestBody MarkOrderReadyForDeliveryRequest markOrderReadyForDelivery) {
        try {
            LOGGER.info("markOrderReadyForDelivery(): Request received to mark the order ready for delivery for payload:{} and authToken: {}", toJson(markOrderReadyForDelivery), authToken);
            workerActionService.markOrderReadyForDelivery(authToken, markOrderReadyForDelivery);
            return new ResponseEntity<>(new MarkOrderReadyForDeliveryResponse(true), HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

}
