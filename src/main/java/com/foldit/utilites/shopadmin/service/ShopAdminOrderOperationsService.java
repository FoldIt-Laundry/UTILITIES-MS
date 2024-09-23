package com.foldit.utilites.shopadmin.service;

import com.foldit.utilites.dao.IOrderDetails;
import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RecordsValidationException;
import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ServiceNegotiationConfigHolder;
import com.foldit.utilites.negotiationconfigholder.ShopConfigurationHolder;
import com.foldit.utilites.order.model.CostStructure;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.shopadmin.control.ShopAdminOrderOperationsController;
import com.foldit.utilites.shopadmin.model.AddOrderQuantityRequest;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
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
            UpdateResult updateResult = mongoTemplate.updateFirst(query, update, OrderDetails.class);

            if (updateResult.getModifiedCount() != 1) {
                String errorMessage = String.format("addOrderQuantityDetails(): No records gets updated for the query: %s and update: %s and payload: %s", toJson(query), toJson(update), toJson(addOrderQuantityRequest));
                LOGGER.error(errorMessage);
                throw new RecordsValidationException(errorMessage);
            }

            // Send notification to user
            CompletableFuture<Void> sendNotificationToUser = CompletableFuture.supplyAsync(() -> {
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



}
