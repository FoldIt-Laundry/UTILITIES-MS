package com.foldit.utilites.order.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.GetOrderDetailsFromOrderIdReq;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
public class OrdersController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrdersService ordersService;

    @GetMapping("order/getOrderDetailsByOrderId")
    public ResponseEntity<OrderDetails> getOrderDetailsFromOrderId(@RequestHeader(value="authToken") String authToken, @RequestBody GetOrderDetailsFromOrderIdReq orderDetailsFromOrderIdReq) {
        OrderDetails orderDetails;
        try {
            LOGGER.info("getOrderDetailsFromOrderId(): Initiating request to get the order details from reqObj: {} and authToken: {}", toJson(orderDetailsFromOrderIdReq), authToken);
            orderDetails = ordersService.getOrderDetailsFromOrderId(authToken, orderDetailsFromOrderIdReq);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the orderId: {}, Exception: {}", orderDetailsFromOrderIdReq.getOrderId() ,ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("order/getAllCompleteOrderDetailsFromUserId")
    public ResponseEntity<List<OrderDetails>> getAllOrderDetailsFromUserId(@RequestHeader(value="authToken") String authToken, @RequestParam String userId) {
        List<OrderDetails> allOrderDetails;
        try {
            LOGGER.info("getAllOrderDetailsFromUserId(): Initiating request to get the all order details from userId: {} and authToken: {}", userId, authToken);
            allOrderDetails = ordersService.getAllOrderDetailsFromUserId(authToken, userId);
            return new ResponseEntity<>(allOrderDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getAllOrderDetailsFromUserId(): Exception occured while getting all the order details from userId: {}, Exception: {}", userId ,ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("order/placeOrder")
    public ResponseEntity<OrderDetails> placeOrder(@RequestHeader(value="authToken") String authToken, @RequestBody OrderDetails orderDetails) {
        OrderDetails orderDetailsResponseFromMongo;
        try {
            LOGGER.info("placeOrder(): Initiating request to place order with orderDetails: {} and authToken: {}", toJson(orderDetails), authToken);
            orderDetailsResponseFromMongo = ordersService.placeOrder(authToken, orderDetails);
            return new ResponseEntity<>(orderDetailsResponseFromMongo, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while processing the order details, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("order/getAllBasicOrderDetailsFromUserId")
    public ResponseEntity<List<BasicOrderDetails>> getAllBasicOrderDetailsFromUserId(@RequestHeader(value="authToken") String authToken, @RequestParam String userId) {
        List<BasicOrderDetails> metaDataOrderDetailsInUserCollections;
        try {
            LOGGER.info("getAllBasicOrderDetailsFromUserId(): Initating request to get user basic order history details from userId: {} and authToken: {}", userId, authToken);
            metaDataOrderDetailsInUserCollections = ordersService.getUserOrderHistoryFromUserId(authToken, userId);
            return new ResponseEntity<>(metaDataOrderDetailsInUserCollections, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("getAllBasicOrderDetailsFromUserId(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }



}
