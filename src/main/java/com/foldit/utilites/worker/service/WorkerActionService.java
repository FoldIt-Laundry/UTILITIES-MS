package com.foldit.utilites.worker.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.WorkflowTransitionDetails;
import com.foldit.utilites.store.model.StoreDetails;
import com.foldit.utilites.tokenverification.service.TokenValidationService;
import com.foldit.utilites.user.model.UserDetails;
import com.foldit.utilites.worker.model.ApproveOrderRequest;
import com.foldit.utilites.worker.model.MarkOrderReadyForDeliveryRequest;
import com.foldit.utilites.worker.model.MarkWorkInProgressRequest;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.*;

@Service
public class WorkerActionService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(WorkerActionService.class);

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
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private TokenValidationService tokenValidationService;

    @Transactional(readOnly = true)
    public List<OrderDetails> getAllUnApprovedOrderList(String workedId, String authToken) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, workedId);
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Arrays.asList(workedId));
            return iOrderDetails.getAllUnApprovedOrderList(storeDetails.getId(), String.valueOf(PENDING_WORKER_APPROVAL));
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getAllUnApprovedOrderList(): Exception occurred while reading the order details from workerId: {} and authToken: {}from monogoDb, Exception: %s", workedId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void approvePendingOrder(String authToken, ApproveOrderRequest approveOrderRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, approveOrderRequest.getWorkerId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Arrays.asList(approveOrderRequest.getWorkerId()));
            if(storeDetails.getId().equalsIgnoreCase(approveOrderRequest.getStoreId())) {
                String errorMessage = String.format("approvePendingOrder(): Given workerId: %s is not entitled to approve the order for the storeId: %s, It is entitled to authorize for the store: %s", approveOrderRequest.getWorkerId(), approveOrderRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(approveOrderRequest.getWorkerId())
                    .where("storeId").is(approveOrderRequest.getStoreId())
                    .where("userWorkflowStatus").is(ORDER_PLACED)
                    .where("workerRiderWorkflowStatus").is(PENDING_WORKER_APPROVAL));
            Update update = new Update()
                    .set("userWorkflowStatus", ACCEPTED)
                    .set("workerRiderWorkflowStatus", ASSIGNED_FOR_RIDER_PICKUP)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(approveOrderRequest.getWorkerId(), ORDER_PLACED + " " + PENDING_WORKER_APPROVAL, istTime.toLocalDateTime(), ACCEPTED + " " + ASSIGNED_FOR_RIDER_PICKUP));

            UpdateResult updateResult =  mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if(updateResult.getModifiedCount()!=1) {
                String errorMessage = String.format("approvePendingOrder(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(approveOrderRequest.getOrderId());
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, USER_UPDATE_ORDER_ACCEPTED));
                return null;
            });

            // Send notification to rider and admin
            CompletableFuture<StoreDetails> getRiderAndAdminUserIdsFromStoreDetails = CompletableFuture.supplyAsync(() -> iStoreDetails.getRiderAndShopAdminIds(approveOrderRequest.getStoreId()));

            CompletableFuture<Void> sendNotificationToWorker = getRiderAndAdminUserIdsFromStoreDetails.thenApplyAsync((storeDetailsForWorkerUserIds) -> {
                storeDetailsForWorkerUserIds.getShopRiderIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, RIDER_ORDER_ASSIGNED_FOR_PICKUP));
                });
                return null;
            });

            CompletableFuture<Void> sendNotificationToAdmin = getRiderAndAdminUserIdsFromStoreDetails.thenApplyAsync((storeDetailsForAdminUserIds) -> {
                storeDetailsForAdminUserIds.getShopAdminIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_ACCEPTED_REQUEST_UPDATE));
                });
                return null;
            });


        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("approvePendingOrder(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(approveOrderRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    @Transactional
    public void markWorkInProgress(String authToken, MarkWorkInProgressRequest markWorkInProgressRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, markWorkInProgressRequest.getWorkerId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Collections.singletonList(markWorkInProgressRequest.getWorkerId()));
            if (storeDetails.getId().equalsIgnoreCase(markWorkInProgressRequest.getStoreId())) {
                String errorMessage = String.format("markWorkInProgress(): Given workerId: %s is not entitled to mark any order status for the storeId: %s, It is entitled to authorize for the store: %s", markWorkInProgressRequest.getWorkerId(), markWorkInProgressRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(markWorkInProgressRequest.getOrderId())
                    .where("storeId").is(markWorkInProgressRequest.getStoreId())
                    .where("userWorkflowStatus").is(ACCEPTED)
                    .where("workerRiderWorkflowStatus").is(PICKED_UP));
            Update update = new Update()
                    .set("userWorkflowStatus", MAGIC_IN_PROGRESS)
                    .set("workerRiderWorkflowStatus", IN_STORE)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(markWorkInProgressRequest.getWorkerId(), ACCEPTED + " " + PICKED_UP, istTime.toLocalDateTime(), MAGIC_IN_PROGRESS + " " + IN_STORE));

            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if (updateResult.getModifiedCount() != 1) {
                String errorMessage = String.format("markWorkInProgress(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(markWorkInProgressRequest.getOrderId());
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, USER_UPDATE_MAGIC_IN_PROGRESS));
                return null;
            });

            // Send notification to Admin
            CompletableFuture<Void> sendNotificationToRiderAndAdmin = CompletableFuture.supplyAsync(() -> {
                StoreDetails shopWorkerAdminIds = iStoreDetails.getShopAdminIds(markWorkInProgressRequest.getStoreId());
                shopWorkerAdminIds.getShopAdminIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_WORK_IN_PROGRESS_REQUEST_UPDATE));
                });
                return null;
            });

        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markWorkInProgress(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(markWorkInProgressRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public void markOrderReadyForDelivery(String authToken, MarkOrderReadyForDeliveryRequest markOrderReadyForDeliveryRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, markOrderReadyForDeliveryRequest.getWorkerId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Collections.singletonList(markOrderReadyForDeliveryRequest.getWorkerId()));
            if (storeDetails.getId().equalsIgnoreCase(markOrderReadyForDeliveryRequest.getStoreId())) {
                String errorMessage = String.format("markOrderReadyForDelivery(): Given workerId: %s is not entitled to mark any order status for the storeId: %s, It is entitled to authorize for the store: %s", markOrderReadyForDeliveryRequest.getWorkerId(), markOrderReadyForDeliveryRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(markOrderReadyForDeliveryRequest.getOrderId())
                    .where("storeId").is(markOrderReadyForDeliveryRequest.getStoreId())
                    .where("userWorkflowStatus").is(MAGIC_IN_PROGRESS)
                    .where("workerRiderWorkflowStatus").is(IN_STORE));
            Update update = new Update()
                    .set("userWorkflowStatus", READY_FOR_DELIVERY)
                    .set("workerRiderWorkflowStatus", READY_FOR_DELIVERY)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(markOrderReadyForDeliveryRequest.getWorkerId(), MAGIC_IN_PROGRESS + " " + IN_STORE, istTime.toLocalDateTime(), READY_FOR_DELIVERY + " " + READY_FOR_DELIVERY));

            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if (updateResult.getModifiedCount() != 1) {
                String errorMessage = String.format("markOrderReadyForDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Get rider and admin user ids
            CompletableFuture<StoreDetails> getRiderAndAdminUserIdsFromStoreDetails = CompletableFuture.supplyAsync(() -> iStoreDetails.getRiderAndShopAdminIds(markOrderReadyForDeliveryRequest.getStoreId()));

            // Send notification to worker
            CompletableFuture<Void> sendNotificationToRider = getRiderAndAdminUserIdsFromStoreDetails.thenApplyAsync((storeDetailsForWorkerUserIds) -> {
                storeDetailsForWorkerUserIds.getShopRiderIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, RIDER_ORDER_READY_FOR_DELIVERY));
                });
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = getRiderAndAdminUserIdsFromStoreDetails.thenApplyAsync((storeDetailsForAdminUserIds) -> {
                storeDetailsForAdminUserIds.getShopAdminIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_READY_FOR_DELIVERY));
                });
                return null;
            });

        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markWorkInProgress(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(markWorkInProgressRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
