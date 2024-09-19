package com.foldit.utilites.firebase.service;

import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@Service
public class FireBaseMessageSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FireBaseMessageSenderService.class);

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public void sendPushNotification(NotificationMessageRequest notificationMessageRequest) {
        try {
            Notification notification = Notification
                    .builder()
                    .setTitle(notificationMessageRequest.getTitle())
                    .setBody(notificationMessageRequest.getBody())
                    .build();
            Message message = Message
                    .builder()
                    .setToken(notificationMessageRequest.getFcmToken())
                    .setNotification(notification)
                    .putAllData(notificationMessageRequest.getData())
                    .build();
            LOGGER.info("sendPushNotification(): Sending push notification with payload: {}", toJson(notificationMessageRequest));
            firebaseMessaging.send(message);
        } catch (Exception ex) {
            LOGGER.error("sendPushNotification(): Exception occurred while sending push notifications for payload:{} and Exception: {}, Not throwing exception", toJson(notificationMessageRequest), ex.getMessage());
        }
    }

}
