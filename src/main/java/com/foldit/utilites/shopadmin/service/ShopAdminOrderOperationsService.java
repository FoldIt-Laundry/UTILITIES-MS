package com.foldit.utilites.shopadmin.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.*;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.CostStructure;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.order.model.WorkflowTransitionDetails;
import com.foldit.utilites.notification.interfaces.ISendNotification;
import com.foldit.utilites.notification.model.NotificationRequest;
import com.foldit.utilites.notification.service.FireBaseNotificationService;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.redisdboperation.service.OrderOperationsInSlotQueueService;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import com.foldit.utilites.rider.model.PickUpAndDeliverySlotsResponse;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.shopadmin.control.ShopAdminOrderOperationsController;
import com.foldit.utilites.shopadmin.model.*;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.foldit.utilites.constant.OrderRelatedConstant.*;
import static com.foldit.utilites.constant.TimeStamp.istTime;
import static com.foldit.utilites.helper.CalculateBillDetails.getFinalBillDetailsFromQuantity;
import static com.foldit.utilites.helper.DateOperations.isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot;
import static com.foldit.utilites.helper.JsonPrinter.toJson;
import static com.foldit.utilites.order.model.WorkflowStatus.ACCEPTED;
import static com.foldit.utilites.order.model.WorkflowStatus.ASSIGNED_FOR_RIDER_PICKUP;

@Service
public class ShopAdminOrderOperationsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ShopAdminOrderOperationsController.class);

    @Autowired
    private ShopConfigurationHolder shopConfigurationHolder;
    @Autowired
    private NegotiationConfigHolder negotiationConfigHolder;
    @Autowired
    private TokenValidationService tokenValidationService;
    @Autowired
    private IOrderDetails iOrderDetails;
    @Autowired
    private IUserDetails iUserDetails;
    @Autowired
    private MongoTemplate mongoTemplate;

    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;
    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;
    private final ISendNotification iSendNotification;

    public ShopAdminOrderOperationsService(@Autowired FireBaseNotificationService notificationService, @Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup, @Autowired OrderOperationsInSlotQueueService orderOperationsInSlotQueue) {
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
        this.orderOperationsInSlotQueue = orderOperationsInSlotQueue;
        this.iSendNotification = notificationService;
    }

    @Transactional
    public void addOrderQuantityDetails(String authToken, AddOrderQuantityRequest addOrderQuantityRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, addOrderQuantityRequest.getAdminId());
            if (!shopConfigurationHolder.getStoreAdminIds().contains(addOrderQuantityRequest.getAdminId())) {
                String errorMessage = String.format("addOrderQuantityDetails(): Given admin: %s is not entitled to mark any order status for the storeId: %s", addOrderQuantityRequest.getAdminId(), negotiationConfigHolder.getDefaultShopId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            CostStructure billDetails = getFinalBillDetailsFromQuantity(shopConfigurationHolder.getServiceIdVsServicePrice(), addOrderQuantityRequest.getServiceIdVsServiceQuantity());

            Query query = new Query(Criteria.
                    where("_id").is(addOrderQuantityRequest.getOrderId()));
            Update update = new Update()
                    .set("billDetails", billDetails);


            // Add order quantity details
            CompletableFuture<Void> addOrderQuantityDetails = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != 1) {
                    String errorMessage = String.format("addOrderQuantityDetails(): No records gets updated for the query: %s and update: %s and payload: %s", toJson(query), toJson(update), toJson(addOrderQuantityRequest));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                return  null;
            });

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = addOrderQuantityDetails.thenApplyAsync((Void) -> {
                OrderDetails orderDetails = iOrderDetails.getUserIdFromOrderId(addOrderQuantityRequest.getOrderId());
                iSendNotification.sendToUser(new NotificationRequest(ORDER_UPDATE, String.format(USER_UPDATE_ORDER_QUANTITY_DETAILS_UPDATED, billDetails.getFinalPrice())), orderDetails.getUserId());
                return null;
            });

        } catch (RecordsValidationException ex) {
            throw new AuthTokenValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("addOrderQuantityDetails(): Exception occurred while performing read and write operation for request: {} and authToken: {} from monogoDb, Exception: %s", toJson(addOrderQuantityRequest), authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public PickUpAndDeliverySlotsResponse getPickUpDropTimeSlots(String authToken, String adminId) {
        PickUpAndDeliverySlotsResponse pickUpAndDeliverySlotsResponse = new PickUpAndDeliverySlotsResponse();
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, adminId);
            if(!shopConfigurationHolder.getStoreAdminIds().contains(adminId)) {
                throw new AuthTokenValidationException(null);
            }
            pickUpAndDeliverySlotsResponse.setTimeSlots(iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime()));
            return pickUpAndDeliverySlotsResponse;
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("getPickUpDropTimeSlots(): Exception occurred while performing read and write operation for riderId: {} and authToken: {} from monogoDb, Exception: %s", adminId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    @Transactional()
    public void changOrderQueueForRiderPickUpAndDrop(String authToken, ChangeRiderPickUpDeliveryOrderQueue orderQueueRequest) {
        PickUpAndDeliverySlotsResponse pickUpAndDeliverySlotsResponse = new PickUpAndDeliverySlotsResponse();
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, orderQueueRequest.adminId());
            if(!shopConfigurationHolder.getStoreAdminIds().contains(orderQueueRequest.adminId()) || !validateGivenSlotExistOrNot(orderQueueRequest) || orderQueueRequest.indexToChange()==0) {
                LOGGER.error("changOrderQueueForRiderPickUpAndDrop(): Validation failed either the given slot:{} does not exist or adminId is not authorized or index to be added is 0", toJson(orderQueueRequest));
                throw new AuthTokenValidationException(null);
            }
            orderOperationsInSlotQueue.changeTheOrderQueueToMakeDeliveryEfficient(orderQueueRequest);


        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            // LOGGER.error("changOrderQueueForRiderPickUpAndDrop(): Exception occurred while performing read and write operation for riderId: {} and authToken: {} from monogoDb, Exception: %s", adminId, authToken, ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public Map<String,List<OrderDetails>> allOrderListForGivenTimeSlot(String authToken, OrderRequestForAGivenTimeSlot orderRequestForAGivenTimeSlot) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, orderRequestForAGivenTimeSlot.adminId());
            if(!shopConfigurationHolder.getStoreAdminIds().contains(orderRequestForAGivenTimeSlot.adminId()) || !validateGivenSlotExistOrNot2(orderRequestForAGivenTimeSlot) ) {
                LOGGER.error("changOrderQueueForRiderPickUpAndDrop(): Validation failed either the given slot:{} does not exist", toJson(orderRequestForAGivenTimeSlot));
                throw new RecordsValidationException(null);
            }

            List<String> allOrdersIdList = orderOperationsInSlotQueue.getAllTheOrdersIdListPresentInsideGivenSlot(orderRequestForAGivenTimeSlot);
            List<OrderDetails> orderDetailsList = iOrderDetails.findAllById(allOrdersIdList);
            Map<String,List<OrderDetails>> orderDetailsGroupedByTimeSlot = orderDetailsList.parallelStream().collect(Collectors.groupingBy(OrderDetails::getBatchSlotTimingsTime));
            return orderDetailsGroupedByTimeSlot;
        } catch (RedisDBException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (RecordsValidationException ex) {
            throw new RecordsValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("allOrderListForGivenTimeSlot(): Exception occurred while performing read and write operation in database for adminId: {} and authToken: {} from monogoDb and request payload: {}, Exception: %s", orderRequestForAGivenTimeSlot.adminId(), authToken, toJson(orderRequestForAGivenTimeSlot), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

    @Transactional
    public void updateEtaForDeliveryServices(String authToken, UpdateEtaForDeliveryServiceRequest deliveryRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, deliveryRequest.getAdminId());
            if(StringUtils.isNotBlank(deliveryRequest.getServiceId()) && Double.isNaN( deliveryRequest.getServiceTime()) ) {
                String errorMessage = String.format("updateEtaForDeliveryServices(): Input provided is null or corrupt for payload: %s for userId: %s",toJson(deliveryRequest), deliveryRequest.getAdminId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }
            Query query = new Query(Criteria.where("shopAdminIds").in(deliveryRequest.getAdminId())
                    .and("serviceOffered.serviceId").is(deliveryRequest.getServiceId()));
            Update update = new Update().set("serviceOffered.$.serviceTime", deliveryRequest.getServiceTime());
            UpdateResult updateResult = mongoTemplate.updateMulti(query, update, "StoreInformation");
            if(updateResult.getModifiedCount()==0 && updateResult.getMatchedCount()==0 ) {
                throw new MongoDBInsertionException("Not able to find the userID in the shop or serviceId is incorrect");
            }
            shopConfigurationHolder.updateAllServicesInformation();
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (RecordsValidationException ex) {
            throw new RecordsValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("updateEtaForDeliveryServices(): Exception occurred while updating the eta for services data in mongoDB, Exception: %s",  ex.getMessage());
            throw new MongoDBInsertionException(ex.getMessage());
        }
    }


    @Transactional
    public void markOrderOutForDelivery(String authToken, MarkOrderOutForDelivery orderRequest) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, orderRequest.adminId());
            isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot(orderRequest);
            if( !shopConfigurationHolder.getStoreAdminIds().contains(orderRequest.adminId()) || !validateGivenSlotExistOrNot3(orderRequest) ||  !isAdminAllowedToMarkTheOrderOutForPickupForGivenSlot(orderRequest) ) {
                String errorMessage = String.format("markOrderOutForDelivery(): Input provided is null or corrupt for payload: %s for userId: %s",toJson(orderRequest), orderRequest.adminId());
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            OrderRequestForAGivenTimeSlot orderRequestForAGivenTimeSlot = new OrderRequestForAGivenTimeSlot(orderRequest.adminId(), orderRequest.riderDeliveryTask(), orderRequest.timeSlotDate(), orderRequest.timeSlotTime());
            List<String> orderIdsInAGivenSlot = orderOperationsInSlotQueue.getAllTheOrdersIdListPresentInsideGivenSlot(orderRequestForAGivenTimeSlot);


            Query query = new Query().addCriteria(Criteria
                    .where("_id").in(orderIdsInAGivenSlot)
                    .and("userWorkflowStatus").is(ACCEPTED)
                    .and("workerRiderWorkflowStatus").is(ACCEPTED));
            Update update = new Update()
                    .set("userWorkflowStatus", ASSIGNED_FOR_RIDER_PICKUP)
                    .set("workerRiderWorkflowStatus", ASSIGNED_FOR_RIDER_PICKUP)
                    .addToSet("auditForWorkflowChanges", new WorkflowTransitionDetails(orderRequest.adminId(), ACCEPTED + " " + ACCEPTED, istTime.toLocalDateTime(), ASSIGNED_FOR_RIDER_PICKUP + " " + ASSIGNED_FOR_RIDER_PICKUP));

            // Send notification to users for order out for pick up
            CompletableFuture<Void> markStatusOfAllOrderInDb = CompletableFuture.supplyAsync(() -> {
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);
                if (updateResult.getModifiedCount() != orderIdsInAGivenSlot.size()) {
                    String errorMessage = String.format("markOrderOutForDelivery(): No records gets updated for the query: %s and update: %s and for payload:  %s", toJson(query), toJson(update), toJson(orderRequest));
                    LOGGER.error(errorMessage);
                    throw new RecordsValidationException(errorMessage);
                }
                return  null;
            });


            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = markStatusOfAllOrderInDb.thenApplyAsync((Void) -> {
                List<String> userIdsList = iOrderDetails.getAllUserIdFromGivenOrderIdList(orderIdsInAGivenSlot).stream().map(OrderDetails::getUserId).distinct().collect(Collectors.toList());
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, USER_UPDATE_ORDER_OUT_FOR_PICKUP), userIdsList);
                return null;
            });

            CompletableFuture<Void> sendNotificationToRider = markStatusOfAllOrderInDb.thenApplyAsync((Voidd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, RIDER_ORDER_ASSIGNED_FOR_PICKUP), shopConfigurationHolder.getStoreRiderIds());
                return null;
            });

            CompletableFuture<Void> sendNotificationToAdmin = markStatusOfAllOrderInDb.thenApplyAsync((Voiddd) -> {
                iSendNotification.sendToUserList(new NotificationRequest(ORDER_UPDATE, ADMIN_ORDER_ASSIGNED_RIDER_TO_PICKUP), shopConfigurationHolder.getStoreAdminIds());
                return null;
            });

        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (RecordsValidationException ex) {
            throw new RecordsValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("markOrderOutForDelivery(): Exception occurred while updating the eta for services data in mongoDB, Exception: %s",  ex.getMessage());
            throw new MongoDBInsertionException(ex.getMessage());
        }
    }


    public boolean validateGivenSlotExistOrNot(ChangeRiderPickUpDeliveryOrderQueue orderQueueReq) {
        Map<String, Map<RiderDeliveryTask, List<String>>> slotsMap = iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if( slotsMap!=null && slotsMap.containsKey(orderQueueReq.timeSlotDate()) && slotsMap.get(orderQueueReq.timeSlotDate()).containsKey(orderQueueReq.riderDeliveryTask()) && slotsMap.get(orderQueueReq.timeSlotDate()).get(orderQueueReq.riderDeliveryTask()).contains(orderQueueReq.timeSlotTime()) ){
            return true;
        }
        LOGGER.error("validateGivenSlotExistOrNot(): Given slotsMap: {} and inputSlot does not exist: {}", toJson(slotsMap), toJson(orderQueueReq));
        return false;
    }

    public boolean validateGivenSlotExistOrNot2(OrderRequestForAGivenTimeSlot orderRequestForAGivenTimeSlot) {
        Map<String, Map<RiderDeliveryTask, List<String>>> slotsMap = iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if( slotsMap!=null && slotsMap.containsKey(orderRequestForAGivenTimeSlot.timeSlotDate()) && slotsMap.get(orderRequestForAGivenTimeSlot.timeSlotDate()).containsKey(orderRequestForAGivenTimeSlot.riderDeliveryTask()) && slotsMap.get(orderRequestForAGivenTimeSlot.timeSlotDate()).get(orderRequestForAGivenTimeSlot.riderDeliveryTask()).contains(orderRequestForAGivenTimeSlot.timeSlotTime()) ){
            return true;
        }
        LOGGER.error("validateGivenSlotExistOrNot2(): Given slotsMap: {} and inputSlot does not exist: {}", toJson(slotsMap), toJson(orderRequestForAGivenTimeSlot));
        return false;
    }

    public boolean validateGivenSlotExistOrNot3(MarkOrderOutForDelivery markOrderOutForDelivery) {
        Map<String, Map<RiderDeliveryTask, List<String>>> slotsMap = iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if( slotsMap!=null && slotsMap.containsKey(markOrderOutForDelivery.timeSlotDate()) && slotsMap.get(markOrderOutForDelivery.timeSlotDate()).containsKey(markOrderOutForDelivery.riderDeliveryTask()) && slotsMap.get(markOrderOutForDelivery.timeSlotDate()).get(markOrderOutForDelivery.riderDeliveryTask()).contains(markOrderOutForDelivery.timeSlotTime()) ){
            return true;
        }
        LOGGER.error("validateGivenSlotExistOrNot3(): Given slotsMap: {} and inputSlot does not exist: {}", toJson(slotsMap), toJson(markOrderOutForDelivery));
        return false;
    }



}
