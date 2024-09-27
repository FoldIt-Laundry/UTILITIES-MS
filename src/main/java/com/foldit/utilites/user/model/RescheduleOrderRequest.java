package com.foldit.utilites.user.model;

import com.foldit.utilites.order.model.OrderDetails;

public record RescheduleOrderRequest(String userId,
                                     OrderDetails orderDetails,
                                     String newTimeSlotTime,
                                     String newTimeSlotDate) {
}
