package com.foldit.utilites.redisdboperation.interfaces;

import com.foldit.utilites.order.model.OrderDetails;
import com.foldit.utilites.rider.model.NextPickUpOrderDetailsRequest;

public interface OrderOperationsInSlotQueue {

    void addOrderIdInAdditionInSlotQueue(OrderDetails orderDetails);

    String removeAndGetFirstOrderIdFromSlotQueue(NextPickUpOrderDetailsRequest nextPickUpOrderDetailsRequest);

}
