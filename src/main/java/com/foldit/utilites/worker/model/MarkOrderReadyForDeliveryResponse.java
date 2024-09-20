package com.foldit.utilites.worker.model;

public class MarkOrderReadyForDeliveryResponse {

    private boolean markedOrderReadyForDelivery;

    public MarkOrderReadyForDeliveryResponse(boolean markedOrderReadyForDelivery) {
        this.markedOrderReadyForDelivery = markedOrderReadyForDelivery;
    }

    public boolean isMarkedOrderReadyForDelivery() {
        return markedOrderReadyForDelivery;
    }

    public void setMarkedOrderReadyForDelivery(boolean markedOrderReadyForDelivery) {
        this.markedOrderReadyForDelivery = markedOrderReadyForDelivery;
    }
}
