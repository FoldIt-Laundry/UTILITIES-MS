package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.rider.model.NextPickUpOrderDetailsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderOperationsInSlotQueueService implements OrderOperationsInSlotQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderOperationsInSlotQueueService.class);

    @Autowired
    private DatabaseOperationsService databaseOperationsService;

    @Override
    public void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails) {
        try {
            LOGGER.info("addOrderIdInAdditionInSlotQueue(): Initiating request to add orderId: {} in slotTimingDate: {} and slotTimingTime: {}", orderDetails.getId(), orderDetails.getBatchSlotTimingsDate(), orderDetails.getBatchSlotTimingsTime());
            Long rowsAffected = databaseOperationsService.addOrderIdInBatchSlot(orderDetails.getId(), orderDetails.getBatchSlotTimingsDate(), orderDetails.getBatchSlotTimingsTime());
            if(rowsAffected==null || rowsAffected==0) {
                throw new RedisDBException(String.format("Failed to insert orderId: %s in given slot timing: %s ", orderDetails.getId(), orderDetails.getBatchSlotTimingsDate()+orderDetails.getBatchSlotTimingsTime()));
            }
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }

    @Override
    public String removeAndGetFirstOrderIdFromSlotQueue(NextPickUpOrderDetailsRequest pickUpOrderRequest) {
        String orderId;
        try {
            LOGGER.info("removeAndGetFirstOrderIdFromSlotQueue(): Initiating request to remove and get first orderId in slotTimingDate: {} and slotTimingTime: {}", pickUpOrderRequest.getBatchSlotTimingsDate(), pickUpOrderRequest.getBatchSlotTimingsTime());
            orderId = databaseOperationsService.removeAndGetTheFirstOrderIdInBatchSlot(pickUpOrderRequest.getBatchSlotTimingsDate(), pickUpOrderRequest.getBatchSlotTimingsTime());
            return orderId;
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }
}
