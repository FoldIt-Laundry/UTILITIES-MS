package com.foldit.utilites.firebase.model;

import java.util.Map;

public class NotificationMessageRequest {
    private String fcmToken;
    private String title;
    private String body;
    private String image;
    private Map<String,String> data;

    public NotificationMessageRequest(String fcmToken, String title, String body) {
        this.fcmToken = fcmToken;
        this.title = title;
        this.body = body;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
