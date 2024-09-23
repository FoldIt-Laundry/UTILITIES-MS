package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.rider.model.NextPickUpDropOrderDetailsRequest;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.foldit.utilites.helper.DateOperations.batchSizeForSlotsMapping;

@Service
public class OrderOperationsInSlotQueueService implements OrderOperationsInSlotQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderOperationsInSlotQueueService.class);

    @Autowired
    private DatabaseOperationsService databaseOperationsService;

    @Override
    public void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails, NegotiationConfigHolder negotiationConfigHolder, RiderDeliveryTask riderDeliveryTask) {
        String keyForBatch;
        try {
            keyForBatch = getKeyForBatchSize(negotiationConfigHolder, orderDetails.getBatchSlotTimingsDate(), orderDetails.getBatchSlotTimingsTime());
            LOGGER.info("addOrderIdInAdditionInSlotQueue(): Initiating request to add orderId: {} in batchSlot: {} and riderDeliveryTask: {}", orderDetails.getId(), keyForBatch, riderDeliveryTask);
            Long rowsAffected = databaseOperationsService.addOrderIdInBatchSlot(orderDetails.getId(), keyForBatch, riderDeliveryTask);
            if(rowsAffected==null || rowsAffected==0) {
                throw new RedisDBException(String.format("Failed to insert orderId: %s in given slot timing: %s ", orderDetails.getId(), orderDetails.getBatchSlotTimingsDate()+orderDetails.getBatchSlotTimingsTime()));
            }
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }

    @Override
    public String removeAndGetFirstOrderIdFromSlotQueue(NextPickUpDropOrderDetailsRequest pickUpOrderRequest, NegotiationConfigHolder negotiationConfigHolder, RiderDeliveryTask riderDeliveryTask) {
        String keyForBatch;
        String orderId;
        try {
            keyForBatch = getKeyForBatchSize(negotiationConfigHolder, pickUpOrderRequest.getBatchSlotTimingsDate(), pickUpOrderRequest.getBatchSlotTimingsTime());
            LOGGER.info("removeAndGetFirstOrderIdFromSlotQueue(): Initiating request to remove and get first orderId for batchKey: {} and riderDeliveryTask: {}", keyForBatch, riderDeliveryTask);
            orderId = databaseOperationsService.removeAndGetTheFirstOrderIdInBatchSlot(keyForBatch, riderDeliveryTask);
            return orderId;
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }


    private String getKeyForBatchSize(NegotiationConfigHolder negotiationConfigHolder, String slotDate, String slotTime) {
        int batchSizeForSlotsMapping = batchSizeForSlotsMapping(negotiationConfigHolder, slotDate);
        if(batchSizeForSlotsMapping==0) return slotDate;
        return slotDate+slotTime;
    }
}
