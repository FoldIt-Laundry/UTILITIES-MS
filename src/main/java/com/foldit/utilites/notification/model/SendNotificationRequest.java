package com.foldit.utilites.notification.model;

public record SendNotificationRequest(String adminId,
                                      NotificationRequest notificationRequest) {
}
