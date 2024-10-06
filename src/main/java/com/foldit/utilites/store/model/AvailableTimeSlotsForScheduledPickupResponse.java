package com.foldit.utilites.store.model;


import com.foldit.utilites.rider.model.RiderDeliveryTask;

import java.util.List;
import java.util.Map;

public class AvailableTimeSlotsForScheduledPickupResponse {

    private  Map<String,Map<SlotsSegregation,List<String>>> availableSlots;

    public  Map<String,Map<SlotsSegregation,List<String>>> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots( Map<String,Map<SlotsSegregation,List<String>>> availableSlots) {
        this.availableSlots = availableSlots;
    }
}
