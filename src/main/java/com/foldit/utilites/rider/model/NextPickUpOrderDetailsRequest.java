package com.foldit.utilites.rider.model;

public class NextPickUpOrderDetailsRequest {
    private String riderId;
    private String batchSlotTimingsDate;
    private String batchSlotTimingsTime;

    public String getRiderId() {
        return riderId;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public String getBatchSlotTimingsDate() {
        return batchSlotTimingsDate;
    }

    public void setBatchSlotTimingsDate(String batchSlotTimingsDate) {
        this.batchSlotTimingsDate = batchSlotTimingsDate;
    }

    public String getBatchSlotTimingsTime() {
        return batchSlotTimingsTime;
    }

    public void setBatchSlotTimingsTime(String batchSlotTimingsTime) {
        this.batchSlotTimingsTime = batchSlotTimingsTime;
    }
}
