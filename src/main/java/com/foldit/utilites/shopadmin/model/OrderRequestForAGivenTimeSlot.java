package com.foldit.utilites.shopadmin.model;

import com.foldit.utilites.rider.model.RiderDeliveryTask;

public record OrderRequestForAGivenTimeSlot(String adminId,
                                            RiderDeliveryTask riderDeliveryTask,
                                            String timeSlotDate,
                                            String timeSlotTime) {
}
