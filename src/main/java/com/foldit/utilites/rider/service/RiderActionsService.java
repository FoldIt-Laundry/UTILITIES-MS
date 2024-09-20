package com.foldit.utilites.rider.service;

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
import com.foldit.utilites.rider.model.MarkOrderOutForDeliveryRequest;
import com.foldit.utilites.rider.model.OrderDeliveredRequest;
import com.foldit.utilites.store.model.StoreDetails;
import com.foldit.utilites.tokenverification.service.TokenValidationService;
import com.foldit.utilites.user.model.UserDetails;
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

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.*;

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
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private TokenValidationService tokenValidationService;

    @Transactional
    public void markOrderOutForDelivery(String authToken, MarkOrderOutForDeliveryRequest deliveryRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, deliveryRequest.getRiderId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichRiderIsPartOf(Collections.singletonList(deliveryRequest.getRiderId()));
            if (storeDetails.getId().equalsIgnoreCase(deliveryRequest.getStoreId())) {
                String errorMessage = String.format("markOrderOutForDelivery(): Given riderId: %s is not entitled to mark any order status for the storeId: %s, It is entitled to authorize for the store: %s", deliveryRequest.getRiderId(), deliveryRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(deliveryRequest.getOrderId())
                    .where("storeId").is(deliveryRequest.getStoreId())
                    .where("userWorkflowStatus").is(READY_FOR_DELIVERY)
                    .where("workerRiderWorkflowStatus").is(READY_FOR_DELIVERY));
            Update update = new Update()
                    .set("userWorkflowStatus", OUT_FOR_DELIVERY)
                    .set("workerRiderWorkflowStatus", OUT_FOR_DELIVERY)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(deliveryRequest.getRiderId(), READY_FOR_DELIVERY + " " + READY_FOR_DELIVERY, istTime.toLocalDateTime(), OUT_FOR_DELIVERY + " " + OUT_FOR_DELIVERY));

            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if (updateResult.getModifiedCount() != 1) {
                String errorMessage = String.format("markOrderOutForDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(deliveryRequest.getOrderId());
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_OUT_FOR_DELIVERY, String.format(USER_UPDATE_ORDER_OUT_FOR_DELIVERY, orderDetails.getCheckOutOtp())));
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
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichRiderIsPartOf(Collections.singletonList(deliveryRequest.getRiderId()));
            if (storeDetails.getId().equalsIgnoreCase(deliveryRequest.getStoreId())) {
                String errorMessage = String.format("confirmOrderDelivery(): Given riderId: %s is not entitled to mark any order status for the storeId: %s, It is entitled to authorize for the store: %s", deliveryRequest.getRiderId(), deliveryRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(deliveryRequest.getOrderId())
                    .where("storeId").is(deliveryRequest.getStoreId())
                    .where("userWorkflowStatus").is(OUT_FOR_DELIVERY)
                    .where("workerRiderWorkflowStatus").is(OUT_FOR_DELIVERY));
            Update update = new Update()
                    .set("userWorkflowStatus", DELIVERED)
                    .set("workerRiderWorkflowStatus", DELIVERED)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(deliveryRequest.getRiderId(), OUT_FOR_DELIVERY + " " + OUT_FOR_DELIVERY, istTime.toLocalDateTime(), DELIVERED + " " + DELIVERED));

            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if (updateResult.getModifiedCount() != 1) {
                String errorMessage = String.format("confirmOrderDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(deliveryRequest.getOrderId());
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_DELIVERED, USER_UPDATE_ORDER_DELIVERED_SUCCESSFULLY));
                return null;
            });

            // Send notification to Admin
            CompletableFuture<Void> sendNotificationToAdmin = CompletableFuture.supplyAsync(() -> {
                StoreDetails shopWorkerAdminIds = iStoreDetails.getShopAdminIds(deliveryRequest.getStoreId());
                shopWorkerAdminIds.getShopAdminIds().parallelStream().forEach(userId -> {
                    UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                    fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_DELIVERED, USER_UPDATE_ORDER_DELIVERED_SUCCESSFULLY));
                });
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
