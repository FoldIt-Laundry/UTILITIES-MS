package com.foldit.utilites.rider.model;

public class MarkOrderPickedUpResponse {

    private boolean markOrderPickedUp;

    public MarkOrderPickedUpResponse(boolean markOrderPickedUp) {
        this.markOrderPickedUp = markOrderPickedUp;
    }

    public boolean isMarkOrderPickedUp() {
        return markOrderPickedUp;
    }

    public void setMarkOrderPickedUp(boolean markOrderPickedUp) {
        this.markOrderPickedUp = markOrderPickedUp;
    }
}
