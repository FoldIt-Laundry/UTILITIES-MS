package com.foldit.utilites.shopadmin.model;

import com.foldit.utilites.rider.model.RiderDeliveryTask;

public record AllOrderForAGivenSlot(String adminId,
                                    RiderDeliveryTask riderDeliveryTask,
                                    String timeSlotDate,
                                    String timeSlotTime) {
}
