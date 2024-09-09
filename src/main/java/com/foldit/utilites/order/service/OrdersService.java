package com.foldit.utilites.order.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.order.dao.IOrderDetails;
import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.GetOrderDetailsFromOrderIdReq;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;


@Service
public class OrdersService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private IOrderDetails iOrderDetails;


    @Transactional(readOnly = true)
    public OrderDetails getOrderDetailsFromOrderId(String authToken, GetOrderDetailsFromOrderIdReq orderDetailsFromOrderIdReq) {
        try {
            validateAuthToken(orderDetailsFromOrderIdReq.getUserId(), authToken);
            return iOrderDetails.findById(orderDetailsFromOrderIdReq.getOrderId()).orElseGet(() -> (new OrderDetails()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the order details from req object: {} details from monogoDb, Exception: %s", toJson(orderDetailsFromOrderIdReq), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> getAllOrderDetailsFromUserId(String authToken, String userId) {
        try {
            validateAuthToken(userId, authToken);
            return iOrderDetails.getAllOrdersListFromUserId(userId);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while getting all the order details from userId: {} details from monogoDb, Exception: %s", userId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> getAllActiveOrderDetailsFromUserId(String authToken, String userId) {
        try {
            validateAuthToken(userId, authToken);
            return iOrderDetails.getAllActiveOrdersListFromUserId(userId,  "Completed");
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while getting all the order details from userId: {} details from monogoDb, Exception: %s", userId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public OrderDetails placeOrder(String authToken, OrderDetails orderDetails) {
        try {
            validateAuthToken(orderDetails.getUserId(), authToken);
            OrderDetails orderDetailsFromMongo = iOrderDetails.save(orderDetails);
            return orderDetailsFromMongo;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the orderDetails: {} details to monogoDb, Exception: %s", toJson(orderDetails), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<BasicOrderDetails> getUserOrderHistoryFromUserId(String authToken, String userId) {
        List<BasicOrderDetails> userOrderHistoryDetails = null;
        try {
            validateAuthToken(userId, authToken);
            return iOrderDetails.getBasicOrderDetailsFromUserId(userId);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getUserOrderHistoryFromUserId(): Exception occurred getting the user oder history from useId: {}, Exception: %s", userId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    private boolean validateAuthToken(String userId, String authToken) {
        if(!tokenVerificationService.validateAuthToken(userId, authToken)) {
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        }
        return true;
    }

}
