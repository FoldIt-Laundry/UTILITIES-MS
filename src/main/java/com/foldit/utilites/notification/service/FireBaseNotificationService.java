package com.foldit.utilites.notification.service;

import com.foldit.utilites.dao.IUserDetails;
import com.foldit.utilites.notification.interfaces.ISendNotification;
import com.foldit.utilites.notification.model.NotificationRequest;
import com.foldit.utilites.user.model.UserDetails;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@Service
@Slf4j
public class FireBaseNotificationService implements ISendNotification {
    @Autowired
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private IUserDetails iUserDetails;

    @Override
    public void sendToAll(NotificationRequest notificationRequest) {
        try {
            log.info("sendToAll(): Request received to send the promotional message to all user for request: {}", toJson(notificationRequest));
            List<UserDetails> userDetails = iUserDetails.getAllUserFcmToken();
            Notification notification = getNotification(notificationRequest);
            List<Message> messageList = userDetails.parallelStream().map(userInfo -> messageBuilder(userInfo.getFcmToken(), notification, notificationRequest.getData())).collect(Collectors.toList());
            firebaseMessaging.sendEachAsync(messageList);
        } catch (Exception ex) {
            log.error("sendToAll(): Exception occurred while sending notification to all the customer present in db, Exception: {}", ex.getMessage());
        }
    }

    @Override
    public void sendToUser(NotificationRequest notificationRequest, String userId) {
        try {
            log.info("sendToUser(): Request received to send the promotional message to userId: {} for request: {}", userId, toJson(notificationRequest));
            UserDetails userDetails = iUserDetails.getFcmTokenFromUserId(userId);
            Notification notification = getNotification(notificationRequest);
            Message message = messageBuilder(userDetails.getFcmToken(), notification, notificationRequest.getData());
            firebaseMessaging.send(message);
        } catch (Exception ex) {
            log.error("sendToUser(): Exception occurred while sending notification to all the customer present in db, Exception: {}", ex.getMessage());
        }
    }

    @Override
    public void sendToUserList(NotificationRequest notificationRequest, List<String> userIdList) {
        try {
            log.info("sendToUserList(): Request received to send the promotional message to userIdList: {} for request: {}", userIdList, toJson(notificationRequest));
            List<UserDetails> userDetails = iUserDetails.getFcmTokenFromUserIdList(userIdList);
            Notification notification = getNotification(notificationRequest);
            List<Message> messageList = userDetails.parallelStream().map(userInfo -> messageBuilder(userInfo.getFcmToken(), notification, notificationRequest.getData())).collect(Collectors.toList());
            firebaseMessaging.sendEachAsync(messageList);
        } catch (Exception ex) {
            log.error("sendToUserList(): Exception occurred while sending notification to all the customer present in db, Exception: {}", ex.getMessage());
        }
    }

    private Message messageBuilder(String fcmToken, Notification notification, Map<String, String> data) {
        return Message
                .builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(data)
                .build();
    }

    private Notification getNotification(NotificationRequest notificationRequest) {
        return  Notification
                .builder()
                .setTitle(notificationRequest.getTitle())
                .setBody(notificationRequest.getBody())
                .setImage(notificationRequest.getImageUrl())
                .build();
    }
}
