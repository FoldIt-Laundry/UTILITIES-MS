package com.foldit.utilites.rider.model;
public class MarkOrderOutForDeliveryResponse {
    private boolean orderMarked;

    public MarkOrderOutForDeliveryResponse(boolean orderMarked) {
        this.orderMarked = orderMarked;
    }

    public boolean isOrderMarked() {
        return orderMarked;
    }

    public void setOrderMarked(boolean orderMarked) {
        this.orderMarked = orderMarked;
    }
}
