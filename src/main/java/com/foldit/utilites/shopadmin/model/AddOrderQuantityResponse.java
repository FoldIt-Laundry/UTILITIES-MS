package com.foldit.utilites.shopadmin.model;

public class AddOrderQuantityResponse {
    private boolean orderQuantityAdded;

    public AddOrderQuantityResponse(boolean orderQuantityAdded) {
        this.orderQuantityAdded = orderQuantityAdded;
    }

    public boolean isOrderQuantityAdded() {
        return orderQuantityAdded;
    }

    public void setOrderQuantityAdded(boolean orderQuantityAdded) {
        this.orderQuantityAdded = orderQuantityAdded;
    }
}
