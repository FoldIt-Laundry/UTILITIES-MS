package com.foldit.utilites.notification.interfaces;

import com.foldit.utilites.notification.model.NotificationRequest;

import java.util.List;

public interface ISendNotification {

    void sendToAll(NotificationRequest notificationRequest);

    void sendToUser(NotificationRequest notificationRequest, String userId);

    void sendToUserList(NotificationRequest notificationRequest, List<String> userId);

}
