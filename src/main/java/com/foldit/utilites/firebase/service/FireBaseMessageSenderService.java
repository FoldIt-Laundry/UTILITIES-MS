package com.foldit.utilites.firebase.service;

import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FireBaseMessageSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FireBaseMessageSenderService.class);

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    public void sendNotification(NotificationMessageRequest notificationMessageRequest) {
        try {
            Notification notification = Notification
                    .builder()
                    .setTitle(notificationMessageRequest.getTitle())
                    .setBody(notificationMessageRequest.getBody())
                    // .setImage(notificationMessageRequest.getImage())
                    .build();

            Message message = Message
                    .builder()
                    .setToken(notificationMessageRequest.getFcmToken())
                    .setNotification(notification)
                    .putAllData(notificationMessageRequest.getData())
                    .build();

            LOGGER.info("");
            firebaseMessaging.send(message);

        } catch (Exception ex) {

        }
    }

}
