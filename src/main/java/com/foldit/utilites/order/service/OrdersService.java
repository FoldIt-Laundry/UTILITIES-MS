package com.foldit.utilites.order.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.order.control.OrdersController;
import com.foldit.utilites.order.dao.IOrderDetails;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import com.foldit.utilites.user.model.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private IOrderDetails iOrderDetails;

    public OrderDetails getOrderDetailsFromOrderId(String authToken, String orderId) {
        try {
            if(!tokenVerificationService.validateAuthToken(authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            return iOrderDetails.findById(orderId).orElseGet(() -> (new OrderDetails()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the orderID: {} details from monogoDb, Exception: %s", orderId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
