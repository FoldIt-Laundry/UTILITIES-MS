package com.foldit.utilites.store.model;


import java.util.List;
import java.util.Map;

public class AvailableTimeSlotsForScheduledPickupResponse {

    private Map<String, List<String>> availableSlots;

    public Map<String, List<String>> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Map<String, List<String>> availableSlots) {
        this.availableSlots = availableSlots;
    }
}
