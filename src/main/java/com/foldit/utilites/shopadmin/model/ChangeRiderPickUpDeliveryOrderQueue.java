package com.foldit.utilites.shopadmin.model;

import com.foldit.utilites.rider.model.RiderDeliveryTask;

public record ChangeRiderPickUpDeliveryOrderQueue(String adminId,
          RiderDeliveryTask riderDeliveryTask,
          String timeSlotDate,
          String timeSlotTime,
                                                  String orderId,
                                                  int indexToChange) {
}
