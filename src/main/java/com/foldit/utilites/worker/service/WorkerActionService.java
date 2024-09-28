package com.foldit.utilites.worker.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IStoreDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
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
import com.foldit.utilites.store.model.StoreDetails;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.*;
import static com.foldit.utilites.rider.model.RiderDeliveryTask.DROP;

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
    private ShopConfigurationHolder shopConfigurationHolder;
    @Autowired
    private TokenValidationService tokenValidationService;
    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;
    private final ISendNotification iSendNotification;

    public WorkerActionService(@Autowired OrderOperationsInSlotQueueService orderIdService, @Autowired FireBaseNotificationService fireBaseNotificationService) {
        this.orderOperationsInSlotQueue = orderIdService;
        this.iSendNotification = fireBaseNotificationService;
    }

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
            String defaultShop = negotiationConfigHolder.getDefaultShopId();
            tokenValidationService.authTokenValidationFromUserId(authToken, approveOrderRequest.getWorkerId());
            StoreDetails storeDetails = iStoreDetails.getShopIdWhichWorkerIsPartOf(Arrays.asList(approveOrderRequest.getWorkerId()));
            if(storeDetails.getId().equalsIgnoreCase(approveOrderRequest.getStoreId())) {
                String errorMessage = String.format("approvePendingOrder(): Given workerId: %s is not entitled to approve the order for the storeId: %s, It is entitled to authorize for the store: %s", approveOrderRequest.getWorkerId(), approveOrderRequest.getStoreId(), storeDetails.getId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }


            Query query = new Query().addCriteria(Criteria
                    .where("_id").is(approveOrderRequest.getWorkerId())
                    .and("storeId").is(defaultShop)
                    .and("userWorkflowStatus").is(ORDER_PLACED)
                    .and("workerRiderWorkflowStatus").is(PENDING_WORKER_APPROVAL));
            Update update = new Update()
                    .set("userWorkflowStatus", ACCEPTED)
                    .set("workerRiderWorkflowStatus", ACCEPTED)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(approveOrderRequest.getWorkerId(), ORDER_PLACED + " " + PENDING_WORKER_APPROVAL, istTime.toLocalDateTime(), ACCEPTED + " " + ACCEPTED));


            // Approve pending order
            CompletableFuture<Void> approvePendingOrder = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult =  mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if(updateResult.getModifiedCount()!=1) {
                    String errorMessage = String.format("approvePendingOrder(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = approvePendingOrder.thenApplyAsync((Void) -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(approveOrderRequest.getOrderId());
                iSendNotification.sendToUser(new NotificationRequest(ORDER_UPDATE, USER_UPDATE_ORDER_ACCEPTED), orderDetails.getUserId());
                return null;
            });

            CompletableFuture<Void> sendNotificationToAdmin = approvePendingOrder.thenApplyAsync((Voiddd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, ADMIN_ORDER_ACCEPTED_REQUEST_UPDATE), shopConfigurationHolder.getStoreAdminIds());
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
            String storeId = negotiationConfigHolder.getDefaultShopId();
            if (!shopConfigurationHolder.getStoreWorkerIds().contains(markWorkInProgressRequest.getWorkerId())) {
                String errorMessage = String.format("markWorkInProgress(): Given workerId: %s is not entitled to mark any order status for the storeId: %s", markWorkInProgressRequest.getWorkerId(), storeId);
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.
                    where("_id").is(markWorkInProgressRequest.getOrderId())
                    .and("storeId").is(markWorkInProgressRequest.getStoreId())
                    .and("userWorkflowStatus").is(ACCEPTED)
                    .and("workerRiderWorkflowStatus").is(ORDER_PICKED_UP));
            Update update = new Update()
                    .set("userWorkflowStatus", MAGIC_IN_PROGRESS)
                    .set("workerRiderWorkflowStatus", IN_STORE)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(markWorkInProgressRequest.getWorkerId(), ACCEPTED + " " + ORDER_PICKED_UP, istTime.toLocalDateTime(), MAGIC_IN_PROGRESS + " " + IN_STORE));



            // Mark work in progress
            CompletableFuture<Void> markOrderWorkInProgress = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("markWorkInProgress(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = markOrderWorkInProgress.thenApplyAsync((Void) -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(markWorkInProgressRequest.getOrderId());
                iSendNotification.sendToUser(new NotificationRequest(ORDER_UPDATE, USER_UPDATE_MAGIC_IN_PROGRESS), orderDetails.getUserId());
                return null;
            });

            // Send notification to Admin
            CompletableFuture<Void> sendNotificationToAdmin = markOrderWorkInProgress.thenApplyAsync((Voidd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, ADMIN_ORDER_WORK_IN_PROGRESS_REQUEST_UPDATE), shopConfigurationHolder.getStoreAdminIds());
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

    @Transactional
    public void markOrderReadyForDelivery(String authToken, MarkOrderReadyForDeliveryRequest markOrderReadyForDeliveryRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, markOrderReadyForDeliveryRequest.getWorkerId());
            String storeId = negotiationConfigHolder.getDefaultShopId();
            if (!shopConfigurationHolder.getStoreWorkerIds().contains(markOrderReadyForDeliveryRequest.getWorkerId())) {
                String errorMessage = String.format("markWorkInProgress(): Given workerId: %s is not entitled to mark any order status for the storeId: %s", markOrderReadyForDeliveryRequest.getWorkerId(), storeId);
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            OrderDetails orderDetails = iOrderDetails.findById(markOrderReadyForDeliveryRequest.getOrderId()).get();

            Query query = new Query(Criteria.
                    where("_id").is(markOrderReadyForDeliveryRequest.getOrderId())
                    .and("storeId").is(markOrderReadyForDeliveryRequest.getStoreId())
                    .and("userWorkflowStatus").is(MAGIC_IN_PROGRESS)
                    .and("workerRiderWorkflowStatus").is(IN_STORE));
            Update update = new Update()
                    .set("userWorkflowStatus", READY_FOR_DELIVERY)
                    .set("workerRiderWorkflowStatus", READY_FOR_DELIVERY)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(markOrderReadyForDeliveryRequest.getWorkerId(), MAGIC_IN_PROGRESS + " " + IN_STORE, istTime.toLocalDateTime(), READY_FOR_DELIVERY + " " + READY_FOR_DELIVERY));


            // Mark Order Ready For Delivery
            CompletableFuture<Void> markOrderReadyForDelivery = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("markOrderReadyForDelivery(): No records gets updated for the query: %s and update: %s", toJson(query), toJson(update));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                orderOperationsInSlotQueue.addOrderIdInAdditionInSlotQueue(orderDetails, DROP);
                return  null;
            });


            // Send notification to worker
            CompletableFuture<Void> sendNotificationToRider = markOrderReadyForDelivery.thenApplyAsync((Void) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, RIDER_ORDER_READY_FOR_DELIVERY), shopConfigurationHolder.getStoreWorkerIds());
                return null;
            });

            // Send notification to admin
            CompletableFuture<Void> sendNotificationToAdmin = markOrderReadyForDelivery.thenApplyAsync((Void) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, ADMIN_ORDER_READY_FOR_DELIVERY), shopConfigurationHolder.getStoreAdminIds());
                return null;
            });

        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderReadyForDelivery(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(markOrderReadyForDeliveryRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
