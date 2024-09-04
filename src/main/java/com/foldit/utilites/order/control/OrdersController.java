package com.foldit.utilites.order.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.service.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrdersController {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private OrdersService ordersService;

    @GetMapping("order/getOrderDetailsByOrderId")
    public ResponseEntity<OrderDetails> getOrderDetailsFromOrderId(@RequestParam String authToken, @RequestParam String orderId) {
        OrderDetails orderDetails;
        try {
            LOGGER.info("getOrderDetailsFromOrderId(): Initiating request to get the order details from orderId: {} and authToken: {}", orderId, authToken);
            orderDetails = ordersService.getOrderDetailsFromOrderId(authToken, orderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }



}
