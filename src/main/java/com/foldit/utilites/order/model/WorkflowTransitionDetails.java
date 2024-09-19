package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Document(collection = "OrderDetails")
public class WorkflowTransitionDetails {
    private String userId;
    private String statusCurrent;
    private LocalDateTime currenTimeStamp;
    private String statusMarked;

    public WorkflowTransitionDetails(String userId, String statusCurrent, LocalDateTime currenTimeStamp, String statusMarked) {
        this.userId = userId;
        this.statusCurrent = statusCurrent;
        this.currenTimeStamp = currenTimeStamp;
        this.statusMarked = statusMarked;
    }

    public LocalDateTime getCurrenTimeStamp() {
        return currenTimeStamp;
    }

    public void setCurrenTimeStamp(LocalDateTime currenTimeStamp) {
        this.currenTimeStamp = currenTimeStamp;
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
