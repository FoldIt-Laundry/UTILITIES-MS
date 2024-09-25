package com.foldit.utilites.shopadmin.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.model.PickUpAndDeliverySlotsResponse;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityRequest;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityResponse;
import com.foldit.utilites.shopadmin.model.AllOrderForAGivenSlot;
import com.foldit.utilites.shopadmin.model.ChangeRiderPickUpDeliveryOrderQueue;
import com.foldit.utilites.shopadmin.service.ShopAdminOrderOperationsService;
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
public class ShopAdminOrderOperationsController {
    private static final Logger LOGGER =  LoggerFactory.getLogger(ShopAdminOrderOperationsController.class);

    @Autowired
    private ShopAdminOrderOperationsService shopAdminOrderOperationsService;

    @PatchMapping("/admin/addOrderQuantityDetails")
    public ResponseEntity<AddOrderQuantityResponse> addOrderQuantityDetails(@RequestHeader(value="authToken") String authToken, @RequestBody AddOrderQuantityRequest addOrderQuantityRequest) {
        try {
            LOGGER.info("addOrderQuantityDetails(): Request received to add order quantity details: {} and authToken: {}", toJson(addOrderQuantityRequest), authToken);
            shopAdminOrderOperationsService.addOrderQuantityDetails(authToken, addOrderQuantityRequest);
            return new ResponseEntity<>(new AddOrderQuantityResponse(true), HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

    @GetMapping("/admin/getPickUpDropTimeSlots")
    public ResponseEntity<PickUpAndDeliverySlotsResponse> getPickUpDropTimeSlots(@RequestHeader(value="authToken") String authToken, @RequestParam("adminId") String adminId) {
        PickUpAndDeliverySlotsResponse pickUpAndDeliverySlotsResponse;
        try {
            LOGGER.info("getPickUpDropTimeSlots(): Get pickup and drop delivery slots adminId: {} and authToken: {}", adminId, authToken);
            pickUpAndDeliverySlotsResponse = shopAdminOrderOperationsService.getPickUpDropTimeSlots(authToken, adminId);
            return new ResponseEntity<>(pickUpAndDeliverySlotsResponse, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new MongoDBReadException(ex.getMessage(), ex);
        }
    }

    @PatchMapping("/admin/changeRiderOrderPickUpDeliveryOrderQueue")
    public ResponseEntity<Boolean> changeRiderOrderPickUpDeliveryOrderQueue(@RequestHeader(value="authToken") String authToken, @RequestBody ChangeRiderPickUpDeliveryOrderQueue changeRiderPickUpDeliveryOrderQueue) {
        try {
            LOGGER.info("changeRiderOrderPickUpDeliveryOrderQueue(): Request received to change the order queue for ider pickup and delivery: {} and authToken: {}", toJson(changeRiderPickUpDeliveryOrderQueue), authToken);
            shopAdminOrderOperationsService.changOrderQueueForRiderPickUpAndDrop(authToken, changeRiderPickUpDeliveryOrderQueue);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while processing the order details, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin/allOrderListForGivenTimeSlot")
    public ResponseEntity<List<OrderDetails>> allOrderListForGivenTimeSlot(@RequestHeader(value="authToken") String authToken, @RequestBody AllOrderForAGivenSlot allOrderForAGivenSlot) {
        List<OrderDetails> orderDetailsList;
        try {
            LOGGER.info("allOrderListForGivenTimeSlot(): Get all order details for a given time slot: {} and authToken: {}", toJson(allOrderForAGivenSlot), authToken);
            orderDetailsList = shopAdminOrderOperationsService.allOrderListForGivenTimeSlot(authToken, allOrderForAGivenSlot);
            return new ResponseEntity<>(orderDetailsList, HttpStatus.OK);
        } catch (RecordsValidationException ex) {
            LOGGER.error("allOrderListForGivenTimeSlot(): Request data validation failed for the input request object: {}, Exception: {}", toJson(allOrderForAGivenSlot), ex.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        } catch (AuthTokenValidationException ex) {
            LOGGER.error("allOrderListForGivenTimeSlot(): Auth Validation failed for adminId: {} and authToken: {}, Exception: {}", allOrderForAGivenSlot.adminId(), authToken, ex.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("allOrderListForGivenTimeSlot(): Exception occurred while processing the details for payload: {}, Exception: {}", toJson(allOrderForAGivenSlot), ex.getMessage());
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.SERVICE_UNAVAILABLE);
        }

    }

}
