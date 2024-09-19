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

    @Transactional(readOnly = true)
    public void approvePendingOrder(String authToken, ApproveOrderRequest approveOrderRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, approveOrderRequest.getWorkerId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Arrays.asList(approveOrderRequest.getWorkerId()));
            if(storeDetails.getId().equalsIgnoreCase(approveOrderRequest.getOrderDetails().getStoreId())) {
                String errorMessage = String.format("approvePendingOrder(): Given workerId: %s is not entitled to approve the order for the storeId: %s, It is entitled to authorize for the store: %s", approveOrderRequest.getOrderDetails(), approveOrderRequest.getOrderDetails().getStoreId(), storeDetails.getId());
                LOGGER.error("approvePendingOrder(): ");
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(approveOrderRequest.getOrderDetails())
                    .where("storeId").is(approveOrderRequest.getOrderDetails().getStoreId())
                    .where("userWorkflowStatus").is(ORDER_PLACED)
                    .where("workerRiderWorkflowStatus").is(PENDING_WORKER_APPROVAL));
            Update update = new Update()
                    .set("userWorkflowStatus", ACCEPTED)
                    .set("workerRiderWorkflowStatus", ASSIGNED_FOR_RIDER_PICKUP)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(approveOrderRequest.getWorkerId(), String.valueOf(PENDING_WORKER_APPROVAL), istTime.toLocalDateTime(), ACCEPTED + " " + ASSIGNED_FOR_RIDER_PICKUP));

            UpdateResult updateResult =  mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if(updateResult.getModifiedCount()!=1) {
                String errorMessage = String.format("approvePendingOrder(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(approveOrderRequest.getOrderDetails().getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, USER_UPDATE_ORDER_ACCEPTED));
                return null;
            });

            // Send notification to rider and admin
            CompletableFuture<Void> sendNotificationToRiderAndAdmin = CompletableFuture.supplyAsync(() -> {
                StoreDetails shopWorkerAdminIds = iStoreDetails.getRiderAndShopAdminIds(negotiationConfigHolder.getDefaultShopId());
                List<String> shopRiderIds = shopWorkerAdminIds.getShopRiderIds();
                List<String> adminUserId = shopWorkerAdminIds.getShopAdminIds();

                CompletableFuture<Void> sendNotificationToWorker = CompletableFuture.supplyAsync(() -> {
                    shopRiderIds.parallelStream().forEach(userId -> {
                        UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                        fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, RIDER_ORDER_ASSIGNED_FOR_PICKUP));
                    });
                    return null;
                });

                CompletableFuture<Void> sendNotificationToAdmin = CompletableFuture.supplyAsync(() -> {
                    adminUserId.parallelStream().forEach(userId -> {
                        UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
                        fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, ADMIN_ORDER_ACCEPTED_REQUEST_UPDATE));
                    });
                    return null;
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

}
