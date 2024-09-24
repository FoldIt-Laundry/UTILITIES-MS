package com.foldit.utilites.rider.model;

import java.util.List;
import java.util.Map;

public class PickUpAndDeliverySlotsResponse {
    private Map<String, Map<RiderDeliveryTask, List<String>>> timeSlots;

    public Map<String, Map<RiderDeliveryTask, List<String>>> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(Map<String, Map<RiderDeliveryTask, List<String>>> timeSlots) {
        this.timeSlots = timeSlots;
    }
}
