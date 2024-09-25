package com.foldit.utilites.shopadmin.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ServiceNegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.CostStructure;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.redisdboperation.service.OrderOperationsInSlotQueueService;
import com.foldit.utilites.rider.model.PickUpAndDeliverySlotsResponse;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.shopadmin.control.ShopAdminOrderOperationsController;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityRequest;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import com.foldit.utilites.shopadmin.model.AllOrderForAGivenSlot;
import com.foldit.utilites.shopadmin.model.ChangeRiderPickUpDeliveryOrderQueue;
import com.foldit.utilites.store.interfaces.IGetTimeSlotsForScheduledPickUp;
import com.foldit.utilites.store.interfacesimp.SlotsGeneratorForScheduledPickup;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.foldit.utilites.constant.OrderRelatedConstant.ORDER_UPDATE;
import static com.foldit.utilites.constant.OrderRelatedConstant.USER_UPDATE_ORDER_QUANTITY_DETAILS_UPDATED;
import static com.foldit.utilites.helper.CalculateBillDetails.getFinalBillDetailsFromQuantity;
import static com.foldit.utilites.helper.JsonPrinter.toJson;

@Service
public class ShopAdminOrderOperationsService {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ShopAdminOrderOperationsController.class);

    @Autowired
    private ServiceNegotiationConfigHolder serviceNegotiationConfigHolder;
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
    private FireBaseMessageSenderService fireBaseMessageSenderService;
    @Autowired
    private MongoTemplate mongoTemplate;

    private IGetTimeSlotsForScheduledPickUp iGetTimeSlotsForScheduledPickUp;
    private OrderOperationsInSlotQueue orderOperationsInSlotQueue;

    public ShopAdminOrderOperationsService(@Autowired SlotsGeneratorForScheduledPickup slotsGeneratorForScheduledPickup, @Autowired OrderOperationsInSlotQueueService orderOperationsInSlotQueue ){
        this.iGetTimeSlotsForScheduledPickUp = slotsGeneratorForScheduledPickup;
        this.orderOperationsInSlotQueue = orderOperationsInSlotQueue;
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

            CostStructure billDetails = getFinalBillDetailsFromQuantity(serviceNegotiationConfigHolder.getServiceIdVsServicePrice(), addOrderQuantityRequest.getServiceIdVsServiceQuantity());

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
                UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(orderDetails.getUserId());
                fireBaseMessageSenderService.sendPushNotification(new NotificationMessageRequest(userDetails.getFcmToken(), ORDER_UPDATE, String.format(USER_UPDATE_ORDER_QUANTITY_DETAILS_UPDATED, billDetails.getFinalPrice())));
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


    @Transactional(readOnly = true)
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
    public List<OrderDetails> allOrderListForGivenTimeSlot(String authToken, AllOrderForAGivenSlot allOrderForAGivenSlot) {
        try {
            tokenValidationService.authTokenValidationFromUserId(authToken, allOrderForAGivenSlot.adminId());
            if(!shopConfigurationHolder.getStoreAdminIds().contains(allOrderForAGivenSlot.adminId()) || !validateGivenSlotExistOrNot2(allOrderForAGivenSlot) ) {
                LOGGER.error("changOrderQueueForRiderPickUpAndDrop(): Validation failed either the given slot:{} does not exist", toJson(allOrderForAGivenSlot));
                throw new RecordsValidationException(null);
            }
            List<String> allOrdersList = orderOperationsInSlotQueue.getAllTheOrdersIdListPresentInsideGivenSlot(allOrderForAGivenSlot);
            return iOrderDetails.findAllById(allOrdersList);
        } catch (RedisDBException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (RecordsValidationException ex) {
            throw new RecordsValidationException(ex.getMessage());
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            LOGGER.error("allOrderListForGivenTimeSlot(): Exception occurred while performing read and write operation in database for adminId: {} and authToken: {} from monogoDb and request payload: {}, Exception: %s", allOrderForAGivenSlot.adminId(), authToken, toJson(allOrderForAGivenSlot), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
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

    public boolean validateGivenSlotExistOrNot2(AllOrderForAGivenSlot allOrderForAGivenSlot) {
        Map<String, Map<RiderDeliveryTask, List<String>>> slotsMap = iGetTimeSlotsForScheduledPickUp.getRiderAdminTimeSlotsForScheduledPickUp(shopConfigurationHolder.getShopOpeningTime(), shopConfigurationHolder.getShopClosingTime());
        if( slotsMap!=null && slotsMap.containsKey(allOrderForAGivenSlot.timeSlotDate()) && slotsMap.get(allOrderForAGivenSlot.timeSlotDate()).containsKey(allOrderForAGivenSlot.riderDeliveryTask()) && slotsMap.get(allOrderForAGivenSlot.timeSlotDate()).get(allOrderForAGivenSlot.riderDeliveryTask()).contains(allOrderForAGivenSlot.timeSlotTime()) ){
            return true;
        }
        LOGGER.error("validateGivenSlotExistOrNot2(): Given slotsMap: {} and inputSlot does not exist: {}", toJson(slotsMap), toJson(allOrderForAGivenSlot));
        return false;
    }



}
