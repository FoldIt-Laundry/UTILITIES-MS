package com.foldit.utilites.worker.model;

public class MarkWorkInProgressResponse {

    public boolean markedOrderStatus;

    public MarkWorkInProgressResponse(boolean markedOrderStatus) {
        this.markedOrderStatus = markedOrderStatus;
    }

    public boolean isMarkedOrderStatus() {
        return markedOrderStatus;
    }

    public void setMarkedOrderStatus(boolean markedOrderStatus) {
        this.markedOrderStatus = markedOrderStatus;
    }
}
