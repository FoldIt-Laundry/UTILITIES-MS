package com.foldit.utilites.worker.model;

public class ApproveOrderResponse {
    private boolean approved;

    public ApproveOrderResponse(boolean approved) {
        this.approved = approved;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
