package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "OrderDetails")
public class WorkflowTransitionDetails {
    private String userId;
    private String statusCurrent;
    private LocalDateTime currentTimeStamp;
    private String statusMarked;

    public WorkflowTransitionDetails(String userId, String statusCurrent, LocalDateTime currentTimeStamp, String statusMarked) {
        this.userId = userId;
        this.statusCurrent = statusCurrent;
        this.currentTimeStamp = currentTimeStamp;
        this.statusMarked = statusMarked;
    }

    public LocalDateTime getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    public void setCurrentTimeStamp(LocalDateTime currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatusCurrent() {
        return statusCurrent;
    }

    public void setStatusCurrent(String statusCurrent) {
        this.statusCurrent = statusCurrent;
    }

    public String getStatusMarked() {
        return statusMarked;
    }

    public void setStatusMarked(String statusMarked) {
        this.statusMarked = statusMarked;
    }
}
