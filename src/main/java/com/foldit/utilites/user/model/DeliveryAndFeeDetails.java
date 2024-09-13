package com.foldit.utilites.user.model;

public class DeliveryAndFeeDetails {
    private Double distanceFromNearestStore;
    private Double deliveryFee;

    public DeliveryAndFeeDetails(Double distanceFromNearestStore, Double deliveryFee) {
        this.distanceFromNearestStore = distanceFromNearestStore;
        this.deliveryFee = deliveryFee;
    }

    public Double getDistanceFromNearestStore() {
        return distanceFromNearestStore;
    }

    public void setDistanceFromNearestStore(Double distanceFromNearestStore) {
        this.distanceFromNearestStore = distanceFromNearestStore;
    }

    public Double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(Double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
}
