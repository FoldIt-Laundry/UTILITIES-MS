package com.foldit.utilites.rider.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.WorkflowTransitionDetails;
import com.foldit.utilites.notification.interfaces.ISendNotification;
import com.foldit.utilites.notification.model.NotificationRequest;
import com.foldit.utilites.notification.service.FireBaseNotificationService;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.redisdboperation.service.OrderOperationsInSlotQueueService;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import com.foldit.utilites.rider.model.*;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
import com.foldit.utilites.user.model.UserDetails;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.DateOperations.validateTheDateFormat;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.*;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.DROP;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.PICKUP;

@Service
public class RiderActionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiderActionsService.class);
    @Autowired
    private IOrderDetails iOrderDetails;
    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private IStoreDetails iStoreDetails;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private ShopConfigurationHolder shopConfigurationHolder;
    @Autowired
    private TokenValidationService tokenValidationService;
    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;
    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;
    private final ISendNotification iSendNotification;

    public RiderActionsService(@Autowired FireBaseNotificationService notificationService, @Autowired OrderOperationsInSlotQueueService orderIdService, @Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup) {
        this.orderOperationsInSlotQueue = orderIdService;
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
        this.iSendNotification = notificationService;
    }

    @Transactional
    public List<OrderDetails> getNextPickUpOrderDetails(String authToken, NextPickUpDropOrderDetailsRequest pickUpOrderRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, pickUpOrderRequest.getRiderId());
            if(!shopConfigurationHolder.getStoreRiderIds().contains(pickUpOrderRequest.getRiderId()) || !validateTheDateFormat(pickUpOrderRequest.getBatchSlotTimingsDate())) {
                LOGGER.error("getNextPickUpOrderDetails(): Validation failed for given request: {} and available riderIds is: {}", toJson(shopConfigurationHolder), toJson(shopConfigurationHolder.getStoreRiderIds()));
                throw new AuthTokenValidationException(null);
            }
            String orderId = orderOperationsInSlotQueue.getFirstOrderIdFromSlotQueue(pickUpOrderRequest, PICKUP);
            if(StringUtils.isNotBlank(orderId)) return Arrays.asList(iOrderDetails.findById(orderId).get());
            return new ArrayList<>();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getNextPickUpOrderDetails(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: {}", toJson(pickUpOrderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public List<OrderDetails> getNextDropOrderDetails(String authToken, NextPickUpDropOrderDetailsRequest dropOrderRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, dropOrderRequest.getRiderId());
            if(!shopConfigurationHolder.getStoreRiderIds().contains(dropOrderRequest.getRiderId()) || !validateTheDateFormat(dropOrderRequest.getBatchSlotTimingsDate())) {
                LOGGER.error("getNextDropOrderDetails(): Validation failed for given request: {} and available riderIds is: {}", toJson(shopConfigurationHolder), toJson(shopConfigurationHolder.getStoreRiderIds()));
                throw new AuthTokenValidationException(null);
            }
            String orderId = orderOperationsInSlotQueue.getFirstOrderIdFromSlotQueue(dropOrderRequest, DROP);
            if(StringUtils.isNotBlank(orderId)) return Arrays.asList(iOrderDetails.findById(orderId).get());
            return new ArrayList<>();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getNextDropOrderDetails(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: {}", toJson(dropOrderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> getAllPickUpOrderDetails(String authToken, String riderId) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, riderId);
            return iOrderDetails.getAllPickUpOrderDetailsFromRiderId(String.valueOf(ASSIGNED_FOR_RIDER_PICKUP));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getAllPickUpOrderDetails(): Exception occurred while performing read and write operation for riderId: {} and authToken: {} from monogoDb, Exception: %s", riderId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDetails> getAllDeliveryOrderDetails(String authToken, String riderId) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, riderId);
            return iOrderDetails.getAllPickUpOrderDetailsFromRiderId(String.valueOf(READY_FOR_DELIVERY));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getAllDeliveryOrderDetails(): Exception occurred while performing read and write operation for riderId: {} and authToken: {} from monogoDb, Exception: %s", riderId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PickUpAndDeliverySlotsResponse getPickUpDropTimeSlots(String authToken, String riderId) {
        PickUpAndDeliverySlotsResponse pickUpAndDeliverySlotsResponse = new PickUpAndDeliverySlotsResponse();
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, riderId);
            if(!shopConfigurationHolder.getStoreRiderIds().contains(riderId)) {
                throw new AuthTokenValidationException(null);
            }
            pickUpAndDeliverySlotsResponse.setTimeSlots(iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime()));
            return pickUpAndDeliverySlotsResponse;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getPickUpDropTimeSlots(): Exception occurred while performing read and write operation for riderId: {} and authToken: {} from monogoDb, Exception: %s", riderId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    @Transactional
    public void markOrderPickedUpFromCustomerHome(String authToken, MarkOrderPickedUpRequest orderRequest) {
        try {
            String shopId = negotiationConfigHolder.getDefaultShopId();
            tokenValidationService.authTokenValidationFromUserId(authToken, orderRequest.getRiderId());
            OrderDetails orderDetails = iOrderDetails.findById(orderRequest.getOrderId()).get();

            if (shopConfigurationHolder.getStoreRiderIds().contains(orderRequest.getRiderId()) || !orderDetails.getWorkerRiderWorkflowStatus().toString().equalsIgnoreCase(String.valueOf(ASSIGNED_FOR_RIDER_PICKUP))) {
                String errorMessage = String.format("markOrderPickedUpFromCustomerHome(): Given riderId: %s is not entitled to mark any order status for the storeId: current order status is: %s and it should be: %s", orderRequest.getRiderId(), shopId, orderDetails.getWorkerRiderWorkflowStatus() , ASSIGNED_FOR_RIDER_PICKUP);
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            Query query = new Query(Criteria
                    .where("_id").is(orderRequest.getOrderId())
                    .and("userWorkflowStatus").is(ACCEPTED)
                    .and("workerRiderWorkflowStatus").is(ASSIGNED_FOR_RIDER_PICKUP));
            Update update = new Update()
                    .set("userWorkflowStatus", ORDER_PICKED_UP)
                    .set("workerRiderWorkflowStatus", ORDER_PICKED_UP)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(orderRequest.getRiderId(), ACCEPTED + " " + ASSIGNED_FOR_RIDER_PICKUP, istTime.toLocalDateTime(), ORDER_PICKED_UP + " " + ORDER_PICKED_UP));

            // Updating order in db operations
            CompletableFuture<OrderDetails> updateOrderPickedUpFromCustomerHomeInDb = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("markOrderPickedUpFromCustomerHome(): No records gets updated for the query: %s and update: %s and for payload:  %s", toJson(query), toJson(update), toJson(orderRequest));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                orderOperationsInSlotQueue.deleteOrderFromBatchSlotQueues(orderDetails, PICKUP);
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Void) -> {
                iSendNotification.sendToUser(new NotificationRequest(ORDER_UPDATE, USER_UPDATE_ORDER_PICKED_UP), orderDetails.getUserId());
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = updateOrderPickedUpFromCustomerHomeInDb.thenApplyAsync((Voidd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, String.format(ADMIN_ORDER_RIDER_PICKED_UP_THE_ORDER, orderRequest.getOrderId())), shopConfigurationHolder.getStoreAdminIds());
                return null;
            });


        } catch (RedisDBException ex) {
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderPickedUpFromCustomerHome(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(orderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void markOrderOutForDelivery(String authToken, MarkOrderOutForDeliveryRequest deliveryRequest) {
        try {
            String storeId = negotiationConfigHolder.getDefaultShopId();
            tokenValidationService.authTokenValidationFromUserId(authToken, deliveryRequest.getRiderId());
            if (!shopConfigurationHolder.getStoreRiderIds().contains(deliveryRequest.getRiderId())) {
                String errorMessage = String.format("markOrderOutForDelivery(): Given riderId: %s is not entitled to mark any order status for the storeId: %s", deliveryRequest.getRiderId(), storeId);
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            Query query = new Query(Criteria.
                    where("_id").is(deliveryRequest.getOrderId())
                    .and("storeId").is(deliveryRequest.getStoreId())
                    .and("userWorkflowStatus").is(READY_FOR_DELIVERY)
                    .and("workerRiderWorkflowStatus").is(READY_FOR_DELIVERY));
            Update update = new Update()
                    .set("userWorkflowStatus", OUT_FOR_DELIVERY)
                    .set("workerRiderWorkflowStatus", OUT_FOR_DELIVERY)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(deliveryRequest.getRiderId(), READY_FOR_DELIVERY + " " + READY_FOR_DELIVERY, istTime.toLocalDateTime(), OUT_FOR_DELIVERY + " " + OUT_FOR_DELIVERY));

            // Mark Order Out For Delivery
            CompletableFuture<Void> markOrderOutForDelivery = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("markOrderOutForDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                return  null;
            });


            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = markOrderOutForDelivery.thenApplyAsync((Void) -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(deliveryRequest.getOrderId());
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                iSendNotification.sendToUser(new NotificationRequest(ORDER_OUT_FOR_DELIVERY, String.format(USER_UPDATE_ORDER_OUT_FOR_DELIVERY, userDetails.getCheckOutOtp())), orderDetails.getUserId());
                return null;
            });


        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderOutForDelivery(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(deliveryRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void confirmOrderDelivery(String authToken, OrderDeliveredRequest deliveryRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, deliveryRequest.getRiderId());
            String storeId = negotiationConfigHolder.getDefaultShopId();
            OrderDetails orderDetails = iOrderDetails.findById(deliveryRequest.getOrderId()).get();

            if (!shopConfigurationHolder.getStoreRiderIds().contains(deliveryRequest.getRiderId()) || !orderDetails.getWorkerRiderWorkflowStatus().toString().equalsIgnoreCase(String.valueOf(OUT_FOR_DELIVERY))) {
                String errorMessage = String.format("confirmOrderDelivery(): Given riderId: %s is not entitled to mark any order status for the storeId: %s", deliveryRequest.getRiderId(), storeId);
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            Query query = new Query(Criteria.
                    where("_id").is(deliveryRequest.getOrderId())
                    .and("storeId").is(deliveryRequest.getStoreId())
                    .and("userWorkflowStatus").is(OUT_FOR_DELIVERY)
                    .and("workerRiderWorkflowStatus").is(OUT_FOR_DELIVERY));
            Update update = new Update()
                    .set("userWorkflowStatus", DELIVERED)
                    .set("workerRiderWorkflowStatus", DELIVERED)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(deliveryRequest.getRiderId(), OUT_FOR_DELIVERY + " " + OUT_FOR_DELIVERY, istTime.toLocalDateTime(), DELIVERED + " " + DELIVERED));

            // Mark Order Delivered
            CompletableFuture<Void> markOrderOutForDelivery = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("confirmOrderDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                orderOperationsInSlotQueue.deleteOrderFromBatchSlotQueues(orderDetails, DROP);
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = markOrderOutForDelivery.thenApplyAsync((Void) -> {
                iSendNotification.sendToUser(new NotificationRequest(ORDER_DELIVERED, USER_UPDATE_ORDER_DELIVERED_SUCCESSFULLY), orderDetails.getUserId());
                return null;
            });

            // Send notification to Admin
            CompletableFuture<Void> sendNotificationToAdmin = markOrderOutForDelivery.thenApplyAsync((Voidd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_DELIVERED, USER_UPDATE_ORDER_DELIVERED_SUCCESSFULLY), shopConfigurationHolder.getStoreAdminIds());
                return null;
            });


        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderOutForDelivery(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(deliveryRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
