package com.foldit.utilites.redisdboperation.interfaces;

import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.model.NextPickUpDropOrderDetailsRequest;
import com.foldit.utilites.rider.model.RiderDeliveryTask;
import com.foldit.utilites.shopadmin.model.AllOrderForAGivenSlot;
import com.foldit.utilites.shopadmin.model.ChangeRiderPickUpDeliveryOrderQueue;

import java.util.List;

public interface OrderOperationsInSlotQueue {

    void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails, RiderDeliveryTask riderDeliveryTask);

    String getFirstOrderIdFromSlotQueue(NextPickUpDropOrderDetailsRequest nextPickUpDropOrderDetailsRequest, RiderDeliveryTask riderDeliveryTask);

    void changeTheOrderQueueToMakeDeliveryEfficient(ChangeRiderPickUpDeliveryOrderQueue changeRiderPickUpDeliveryOrderQueue);

    List<String> getAllTheOrdersIdListPresentInsideGivenSlot(AllOrderForAGivenSlot allOrderForAGivenSlot);

    void deleteOrderFromBatchSlotQueues(OrderDetails orderDetails, RiderDeliveryTask riderDeliveryTask);

}
