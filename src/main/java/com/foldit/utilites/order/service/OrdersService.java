package com.foldit.utilites.order.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.order.dao.IOrderDetails;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.MetaDataOrderDetailsInUserCollection;
import com.foldit.utilites.tokenverification.service.TokenVerificationService;
import com.foldit.utilites.user.dao.IUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.foldit.utilites.helper.JsonPrinter.toJson;


@Service
public class OrdersService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private TokenVerificationService tokenVerificationService;

    @Autowired
    private IOrderDetails iOrderDetails;

    @Autowired
    private IUserDetails iUserDetails;

    public OrderDetails getOrderDetailsFromOrderId(String authToken, String orderId) {
        try {
            validateAuthToken(authToken);
            return iOrderDetails.findById(orderId).orElseGet(() -> (new OrderDetails()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the orderID: {} details from monogoDb, Exception: %s", orderId, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public OrderDetails placeOrder(String authToken, OrderDetails orderDetails) {
        try {
            validateAuthToken(authToken);
            OrderDetails orderDetailsFromMongo = iOrderDetails.save(orderDetails);

            // Need to add few more details

            return orderDetailsFromMongo;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the orderDetails: {} details to monogoDb, Exception: %s", toJson(orderDetails), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public List<MetaDataOrderDetailsInUserCollection> getUserOrderHistoryFromUserId(String authToken, String userId) {
        List<MetaDataOrderDetailsInUserCollection> userOderHistoryDetails;
        try {
            validateAuthToken(authToken);
            userOderHistoryDetails = iUserDetails.findUserOrderDetailsByUserId(userId);
            return userOderHistoryDetails;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            // LOGGER.error("saveNewUserLocation(): Exception occured while saving the orderDetails: {} details to monogoDb, Exception: %s", toJson(orderDetails), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    private boolean validateAuthToken(String authToken) {
        if(!tokenVerificationService.validateAuthToken(authToken)) {
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        }
        return true;
    }

}
