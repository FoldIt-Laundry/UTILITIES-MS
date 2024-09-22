package com.foldit.utilites.order.service;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.GetOrderDetailsFromOrderIdReq;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.store.model.StoreDetails;
import com.foldit.utilites.tokenverification.service.RedisTokenVerificationService;
import com.foldit.utilites.user.model.UserDetails;
import io.grpc.netty.shaded.io.netty.util.concurrent.CompleteFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.ORDER_PLACED;
import static com.foldit.utilites.order.model.WorkflowStatus.PENDING_WORKER_APPROVAL;


@Service
public class OrdersService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private RedisTokenVerificationService redisTokenVerificationService;
    @Autowired
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private IOrderDetails iOrderDetails;
    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private IStoreDetails iStoreDetails;


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

            UserDetails userDetails = iUserDetails.findById(orderDetails.getUserId()).get();

            validateAuthToken(orderDetails.getUserId(), authToken);
            orderDetails.setStoreId(negotiationConfigHolder.getDefaultShopId());
            orderDetails.setCheckOutOtp(userDetails.getCheckOutOtp());
            orderDetails.setUserWorkflowStatus(ORDER_PLACED);
            orderDetails.setWorkerRiderWorkflowStatus(PENDING_WORKER_APPROVAL);

            // Insert order in db
            CompletableFuture<OrderDetails> orderDetailsInsertedInDb =  CompletableFuture.supplyAsync(() -> iOrderDetails.save(orderDetails));

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = orderDetailsInsertedInDb.thenApplyAsync((OrderDetails) -> {
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, USER_UPDATE_ORDER_PLACED));
                return null;
            });

            // Send notification to worker and admin
            CompletableFuture<StoreDetails> getWorkerAndAdminDetails = orderDetailsInsertedInDb.thenApplyAsync((OrderDetails) -> iStoreDetails.getWorkerAndShopAdminIds(negotiationConfigHolder.getDefaultShopId()));

            CompletableFuture<Void> sendNotificationToWorker = getWorkerAndAdminDetails.thenApplyAsync((storeDetailsForWorkerUserIds) -> {
                storeDetailsForWorkerUserIds.getShopWorkerIds().parallelStream().forEach(userId -> {
                    UserDetails workerUserDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(workerUserDetails.getFcmToken(), ORDER_UPDATE, WORKER_ORDER_RECEIVED_REQUEST));
                });
                return null;
            });

            CompletableFuture<Void> sendNotificationToAdmin = getWorkerAndAdminDetails.thenApplyAsync((storeDetailsForAdminUserIds) -> {
                storeDetailsForAdminUserIds.getShopAdminIds().parallelStream().forEach(userId -> {
                    UserDetails adminUserDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(adminUserDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_RECEIVED_REQUEST));
                });
                return null;
            });

            CompletableFuture.allOf(orderDetailsInsertedInDb);
            return orderDetailsInsertedInDb.join();
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
        if(!redisTokenVerificationService.validateAuthToken(userId, authToken)) {
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        }
        return true;
    }

}
