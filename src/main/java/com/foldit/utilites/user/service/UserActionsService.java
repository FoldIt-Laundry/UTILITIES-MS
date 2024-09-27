package com.foldit.utilites.user.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.exception.*;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.WorkflowStatus;
import com.foldit.utilites.order.model.WorkflowTransitionDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.redisdboperation.service.OrderOperationsInSlotQueueService;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.shopadmin.model.MarkOrderOutForDelivery;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
import com.foldit.utilites.store.model.DeliveryFeeCalculatorRequest;
import com.foldit.utilites.store.model.StoreDetails;
import com.foldit.utilites.redisdboperation.service.DatabaseOperationsService;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.user.model.*;
import com.mongodb.client.result.UpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.GoogleMatrixForDeliveryFee.getDeliveryFeeAndDistanceDetails;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.*;
import static com.foldit.utilites.order.model.WorkflowStatus.ORDER_PICKED_UP;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.DROP;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.PICKUP;

@Service
public class UserActionsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(UserActionsService.class);

    @Autowired
    private DatabaseOperationsService databaseOperationsService;
    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private IStoreDetails iStoreDetails;
    @Autowired
    private IOrderDetails iOrderDetails;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private ShopConfigurationHolder shopConfigurationHolder;
    @Autowired
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private MongoTemplate mongoTemplate;
    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;
    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;

    public UserActionsService(@Autowired OrderOperationsInSlotQueueService orderIdService, @Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup){
        this.orderOperationsInSlotQueue = orderIdService;
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
    }

    @Transactional
    public OnBoardNewUserLocation saveNewUserLocation(OnBoardNewUserLocation onBoardNewUserLocation, String authToken) {
        UserLocation userLocation;
        try {
            if(!databaseOperationsService.validateAuthToken(onBoardNewUserLocation.getUserId(), authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            userLocation = onBoardNewUserLocation.getUserLocation();

            DeliveryAndFeeDetails deliveryAndFeeDetails = deliveryFeeCalculatorFromDefaultStoreAddress(userLocation);
            userLocation.setDistanceFromNearestStore(deliveryAndFeeDetails.getDistanceFromNearestStore());
            userLocation.setDeliveryFeeIfApplicable(deliveryAndFeeDetails.getDeliveryFee());

            Query query = new Query(Criteria.where("id").is(onBoardNewUserLocation.getUserId()));
            Update update = new Update().addToSet("locations", onBoardNewUserLocation.getUserLocation());
            if(StringUtils.isNotBlank(onBoardNewUserLocation.getUserName())) {
                update.set("userName", onBoardNewUserLocation.getUserName());
            }
            mongoTemplate.updateFirst(query, update, UserLocation.class);
            return onBoardNewUserLocation;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while saving the new user location, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public UserDetails getUserDetailsFromUserId(String authToken, String userId) {
        try {
            if(!databaseOperationsService.validateAuthToken(userId, authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            return iUserDetails.findById(userId).orElseGet(() -> (new UserDetails()));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the user details from monogoDb, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<UserLocation> getAllUserLocations(String authToken, String userId) {
        try {
            if(!databaseOperationsService.validateAuthToken(userId, authToken)) {
                LOGGER.error("Auth token: {}, Validation failed", authToken);
                throw new AuthTokenValidationException(null);
            }
            return Optional.ofNullable(iUserDetails.getAllUserLocationFromUserId(userId)).orElseGet(() -> new UserDetails(new ArrayList<>())).getLocations();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex){
            LOGGER.error("saveNewUserLocation(): Exception occured while getting the user details from monogoDb, Exception: %s", ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void cancelOnGoingOrder(String authToken, CancelOrderRequest cancelOrderRequest) {
        try {
            OrderDetails orderDetailsFromDb = iOrderDetails.findById(cancelOrderRequest.orderDetails().getId()).get();
            WorkflowStatus orderWorkflowStatus = orderDetailsFromDb.getWorkerRiderWorkflowStatus();
            databaseOperationsService.validateAuthToken(cancelOrderRequest.userId(), authToken);
            if(orderWorkflowStatus.value<5) {
                String errorMessage = String.format("cancelOnGoingOrder(): For userId: %s", cancelOrderRequest.userId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            Query query = new Query(Criteria.where("_id").is(cancelOrderRequest.orderDetails().getId()));
            Update update = new Update()
                    .set("userWorkflowStatus", CANCELLED)
                    .set("workerRiderWorkflowStatus", CANCELLED)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(cancelOrderRequest.userId(), cancelOrderRequest.orderDetails().getUserWorkflowStatus() + " " + cancelOrderRequest.orderDetails().getWorkerRiderWorkflowStatus(), istTime.toLocalDateTime(), CANCELLED + " " + CANCELLED));

            CompletableFuture<OrderDetails> updateOrderPickedUpFromCustomerHomeInDb = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("cancelOnGoingOrder(): No records gets updated for the query: %s and update: %s and for payload:  %s", toJson(query), toJson(update), toJson(cancelOrderRequest));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                orderOperationsInSlotQueue.deleteOrderFromBatchSlotQueues(cancelOrderRequest.orderDetails(), PICKUP);
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Void) -> {
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetailsFromDb.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_CANCELLED, USER_UPDATE_ORDER_CANCELLED));
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Voidd) -> {
                shopConfigurationHolder.getStoreAdminIds().forEach(storeAdminId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(storeAdminId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_CANCELLED, String.format(ADMIN_ORDER_USER_CANCELLED_THE_ORDER, cancelOrderRequest.orderDetails().getId())));
                });
                return null;
            });

        } catch (RedisDBException ex) {
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderPickedUpFromCustomerHome(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(cancelOrderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void rescheduleOnGoingOrder(String authToken, RescheduleOrderRequest rescheduleOrderRequest) {
        try {
            OrderDetails orderDetailsFromDb = iOrderDetails.findById(rescheduleOrderRequest.orderDetails().getId()).get();
            databaseOperationsService.validateAuthToken(rescheduleOrderRequest.userId(), authToken);
            if(orderDetailsFromDb.getWorkerRiderWorkflowStatus().value>4 || orderDetailsFromDb.getUserWorkflowStatus().value>4 || !validateGivenSlotExistOrNot(rescheduleOrderRequest.newTimeSlotDate(), rescheduleOrderRequest.newTimeSlotTime(), PICKUP) ) {
                String errorMessage = String.format("rescheduleOnGoingOrder(): For userId: %s", rescheduleOrderRequest.userId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            Query query = new Query(Criteria.where("_id").is(rescheduleOrderRequest.orderDetails().getId()));
            Update update = new Update()
                    .set("userWorkflowStatus", PENDING_WORKER_APPROVAL)
                    .set("workerRiderWorkflowStatus", PENDING_WORKER_APPROVAL)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(rescheduleOrderRequest.userId(), orderDetailsFromDb.getUserWorkflowStatus() + " " + orderDetailsFromDb.getWorkerRiderWorkflowStatus(), istTime.toLocalDateTime(), PENDING_WORKER_APPROVAL + " " + PENDING_WORKER_APPROVAL));

            CompletableFuture<OrderDetails> updateOrderPickedUpFromCustomerHomeInDb = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("rescheduleOnGoingOrder(): No records gets updated for the query: %s and update: %s and for payload:  %s", toJson(query), toJson(update), toJson(rescheduleOrderRequest));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                orderOperationsInSlotQueue.deleteOrderFromBatchSlotQueues(rescheduleOrderRequest.orderDetails(), PICKUP);
                rescheduleOrderRequest.orderDetails().setBatchSlotTimingsDate(rescheduleOrderRequest.newTimeSlotDate());
                rescheduleOrderRequest.orderDetails().setBatchSlotTimingsTime(rescheduleOrderRequest.newTimeSlotTime());
                orderOperationsInSlotQueue.addOrderIdInAdditionInSlotQueue( rescheduleOrderRequest.orderDetails(),PICKUP);
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Void) -> {
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetailsFromDb.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_RESCHEDULED, USER_UPDATE_ORDER_RESCHEDULED));
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Void) -> {
                shopConfigurationHolder.getStoreAdminIds().forEach(storeAdminId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(storeAdminId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_RESCHEDULED, String.format(ADMIN_ORDER_USER_RESCHEDULED_THE_ORDER, orderDetailsFromDb.getId())));
                });
                return null;
            });

            // Send notification to worker
            CompletableFuture<Void> sendNotificationToWorker = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Void) -> {
                shopConfigurationHolder.getStoreWorkerIds().forEach(storeWorkerId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(storeWorkerId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_RESCHEDULED, WORKER_ORDER_RESCHEDULED_RECEIVED_REQUEST));
                });
                return null;
            });

        } catch (RedisDBException ex) {
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("rescheduleOnGoingOrder(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(rescheduleOrderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    public DeliveryAndFeeDetails deliveryFeeCalculatorFromDefaultStoreAddress(UserLocation userLocation) {
        try {
            StoreDetails storeDetails = iStoreDetails.getShopDeliveryFeeRelatedInformation("66dcbe4b2f87e5390bc4177e");
            DeliveryFeeCalculatorRequest deliveryFeeCalculatorRequest = new DeliveryFeeCalculatorRequest();
            deliveryFeeCalculatorRequest.setSourceLatitude(String.valueOf((userLocation.getLatitude())));
            deliveryFeeCalculatorRequest.setGoogleApiKey(negotiationConfigHolder.getGoogleApiKeyForDistanceMatrix());
            deliveryFeeCalculatorRequest.setSourceLongitude(String.valueOf(userLocation.getLongitude()));
            deliveryFeeCalculatorRequest.setDestinationLatitude(String.valueOf(storeDetails.getStoreLocation().getLocation().getCoordinates().get(0)));
            deliveryFeeCalculatorRequest.setDestinationLongitude(String.valueOf(storeDetails.getStoreLocation().getLocation().getCoordinates().get(1)));
            return getDeliveryFeeAndDistanceDetails(deliveryFeeCalculatorRequest, storeDetails.getDeliveryFeePerKmAfterThreshold(), storeDetails.getFreeDeliveryDistanceAllowed());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            throw new GoogleApiException(ex.getMessage(), ex);
        }
    }

    public boolean validateGivenSlotExistOrNot(String timeSlotDate, String timeSlotTime, RiderDeliveryTask riderDeliveryTask) {
        Map<String, Map<RiderDeliveryTask, List<String>>> slotsMap = iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if( slotsMap!=null && slotsMap.containsKey(timeSlotDate) && slotsMap.get(timeSlotDate).containsKey(riderDeliveryTask) && slotsMap.get(timeSlotDate).get(riderDeliveryTask).contains(timeSlotTime) ){
            return true;
        }
        LOGGER.error("validateGivenSlotExistOrNot3(): Given slotsMap: {} and inputTimeSlotDate: {}, inputTimeSlotTime: {} and RiderDeliveryTask: {}", toJson(slotsMap), timeSlotDate, timeSlotTime, riderDeliveryTask);
        return false;
    }

}
