package com.foldit.utilites.order.service;

import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.BasicOrderDetails;
import com.foldit.utilites.order.model.GetOrderDetailsFromOrderIdReq;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.redisdboperation.service.OrderOperationsInSlotQueueService;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
import com.foldit.utilites.redisdboperation.service.DatabaseOperationsService;
import com.foldit.utilites.user.model.UserDetails;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.ORDER_PLACED;
import static com.foldit.utilites.order.model.WorkflowStatus.PENDING_WORKER_APPROVAL;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.PICKUP;


@Service
public class OrdersService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(OrdersService.class);
    @Autowired
    private DatabaseOperationsService databaseOperationsService;
    @Autowired
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup;
    @Autowired
    private ShopConfigurationHolder shopConfigurationHolder;
    @Autowired
    private IOrderDetails iOrderDetails;
    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private IStoreDetails iStoreDetails;

    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;

    public OrdersService(@Autowired OrderOperationsInSlotQueueService orderIdService){
        this.orderOperationsInSlotQueue = orderIdService;
    }


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

    boolean verifyTheInputSlotsAndTimings(OrderDetails orderDetails) {
        Map<String, List<String>> slotTimingMap = slotsGeneratorForScheduledPickup.getUserTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if(slotTimingMap.containsKey(orderDetails.getBatchSlotTimingsDate()) && slotTimingMap.get(orderDetails.getBatchSlotTimingsDate()).contains(orderDetails.getBatchSlotTimingsTime())) {
            return true;
        }
        return false;
    }

    @Transactional
    public OrderDetails placeOrder(String authToken, OrderDetails orderDetails) {
        try {

            validateAuthToken(orderDetails.getUserId(), authToken);
            if(!verifyTheInputSlotsAndTimings(orderDetails)) {
                String errorMessage = String.format("placeOrder(): Given slot date: %s and time: %s is not supported by system. Please provide correct slots and timings", orderDetails.getBatchSlotTimingsDate(), orderDetails.getBatchSlotTimingsTime());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            UserDetails userDetails = iUserDetails.findById(orderDetails.getUserId()).get();

            orderDetails.setStoreId(negotiationConfigHolder.getDefaultShopId());
            orderDetails.setCheckOutOtp(userDetails.getCheckOutOtp());
            orderDetails.setUserWorkflowStatus(ORDER_PLACED);
            orderDetails.setWorkerRiderWorkflowStatus(PENDING_WORKER_APPROVAL);

            // Save order details in DB
            CompletableFuture<OrderDetails> orderDetailsInsertedInDb =  CompletableFuture.supplyAsync(() -> {
                OrderDetails updatedOrderDetails = iOrderDetails.save(orderDetails);
                orderOperationsInSlotQueue.addOrderIdInAdditionInSlotQueue(updatedOrderDetails, PICKUP);
                return updatedOrderDetails;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = orderDetailsInsertedInDb.thenApplyAsync((OrderDetails) -> {
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, USER_UPDATE_ORDER_PLACED));
                return null;
            });

            // Send notification to worker
            CompletableFuture<Void> sendNotificationToWorker = orderDetailsInsertedInDb.thenApplyAsync((OrderDetails) -> {
                shopConfigurationHolder.getStoreWorkerIds().parallelStream().forEach(userId -> {
                    UserDetails workerUserDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(workerUserDetails.getFcmToken(), ORDER_UPDATE, WORKER_ORDER_RECEIVED_REQUEST));
                });
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = orderDetailsInsertedInDb.thenApplyAsync((OrderDetails) -> {
                shopConfigurationHolder.getStoreAdminIds().parallelStream().forEach(userId -> {
                    UserDetails adminUserDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(adminUserDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_RECEIVED_REQUEST));
                });
                return null;
            });

            return orderDetailsInsertedInDb.get();
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
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
        if(!databaseOperationsService.validateAuthToken(userId, authToken)) {
            LOGGER.error("Auth token: {}, Validation failed", authToken);
            throw new AuthTokenValidationException(null);
        }
        return true;
    }

}
