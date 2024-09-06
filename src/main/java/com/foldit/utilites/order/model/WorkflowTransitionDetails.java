package com.foldit.utilites.order.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OrderDetails")
public class WorkflowTransitionDetails {
    private String userId;
    private String userName;
    private String statusMarked;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatusMarked() {
        return statusMarked;
    }

    public void setStatusMarked(String statusMarked) {
        this.statusMarked = statusMarked;
    }
}
