package com.foldit.utilites.redisdboperation.interfaces;

import com.foldit.utilites.negotiationconfigholder.NegotiationConfigHolder;
import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.model.NextPickUpDropOrderDetailsRequest;
import com.foldit.utilites.rider.model.RiderDeliveryTask;

public interface OrderOperationsInSlotQueue {

    void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails, NegotiationConfigHolder negotiationConfigHolder, RiderDeliveryTask riderDeliveryTask);

    String removeAndGetFirstOrderIdFromSlotQueue(NextPickUpDropOrderDetailsRequest nextPickUpDropOrderDetailsRequest, NegotiationConfigHolder negotiationConfigHolder, RiderDeliveryTask riderDeliveryTask);

}
