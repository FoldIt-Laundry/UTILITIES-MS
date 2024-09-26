package com.foldit.utilites.redisdboperation.service;

import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.redisdboperation.interfaces.OrderOperationsInSlotQueue;
import com.foldit.utilites.rider.model.NextPickUpDropOrderDetailsRequest;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.shopadmin.model.OrderRequestForAGivenTimeSlot;
import com.foldit.utilites.shopadmin.model.ChangeRiderPickUpDeliveryOrderQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderOperationsInSlotQueueService implements OrderOperationsInSlotQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderOperationsInSlotQueueService.class);

    @Autowired
    private DatabaseOperationsService databaseOperationsService;

    @Override
    public void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails, RiderDeliveryTask riderDeliveryTask) {
        String keyForBatch;
        try {
            keyForBatch = orderDetails.getBatchSlotTimingsDate()+orderDetails.getBatchSlotTimingsTime();
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
    @Transactional(readOnly = true)
    public String getFirstOrderIdFromSlotQueue(NextPickUpDropOrderDetailsRequest pickUpOrderRequest, RiderDeliveryTask riderDeliveryTask) {
        String keyForBatch;
        String orderId;
        try {
            keyForBatch = pickUpOrderRequest.getBatchSlotTimingsDate()+ pickUpOrderRequest.getBatchSlotTimingsTime();
            LOGGER.info("removeAndGetFirstOrderIdFromSlotQueue(): Initiating request to remove and get first orderId for batchKey: {} and riderDeliveryTask: {}", keyForBatch, riderDeliveryTask);
            orderId = databaseOperationsService.getTheFirstOrderIdInBatchSlot(keyForBatch, riderDeliveryTask);
            return orderId;
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }

    @Override
    public void changeTheOrderQueueToMakeDeliveryEfficient(ChangeRiderPickUpDeliveryOrderQueue changeRiderPickUpDeliveryOrderQueue) {
        String keyForBatch;
        try {
            keyForBatch = changeRiderPickUpDeliveryOrderQueue.timeSlotDate() + changeRiderPickUpDeliveryOrderQueue.timeSlotTime();
            // LOGGER.info("removeAndGetFirstOrderIdFromSlotQueue(): Initiating request to remove and get first orderId for batchKey: {} and riderDeliveryTask: {}", keyForBatch, riderDeliveryTask);
            databaseOperationsService.changeIndexOfAGivenValueInList(keyForBatch, changeRiderPickUpDeliveryOrderQueue.orderId(), changeRiderPickUpDeliveryOrderQueue.indexToChange(), changeRiderPickUpDeliveryOrderQueue.riderDeliveryTask());
        } catch (Exception ex) {
            throw new RedisDBException(ex.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTheOrdersIdListPresentInsideGivenSlot(OrderRequestForAGivenTimeSlot orderRequestForAGivenTimeSlot) {
        String keyForBatch;
        try {
            keyForBatch = orderRequestForAGivenTimeSlot.timeSlotDate() + orderRequestForAGivenTimeSlot.timeSlotTime();
            return databaseOperationsService.getAllOrderIdsInBatchSlot(keyForBatch, orderRequestForAGivenTimeSlot.riderDeliveryTask());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RedisDBException(ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteOrderFromBatchSlotQueues(OrderDetails orderDetails, RiderDeliveryTask riderDeliveryTask) {
        String keyForBatch;
        try {
            keyForBatch = orderDetails.getBatchSlotTimingsDate() + orderDetails.getBatchSlotTimingsTime();
            databaseOperationsService.deleteAGivenOrderIdInBatchSlot(orderDetails.getId(), keyForBatch, riderDeliveryTask);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new RedisDBException(ex.getMessage(), ex);
        }
    }

}
