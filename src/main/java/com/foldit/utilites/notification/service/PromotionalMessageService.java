package com.foldit.utilites.notification.service;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.exception.MongoDBReadException;
import com.foldit.utilites.exception.RedisDBException;
import com.foldit.utilites.notification.interfaces.ISendNotification;
import com.foldit.utilites.notification.model.SendNotificationRequest;
import com.foldit.utilites.redisdboperation.interfaces.TokenValidation;
import com.foldit.utilites.redisdboperation.service.TokenValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@Service
@Slf4j
public class PromotionalMessageService {

    private final TokenValidation tokenValidation;
    private final ISendNotification sendNotification;

    public PromotionalMessageService(@Autowired TokenValidationService tokenValidation, @Autowired FireBaseNotificationService fireBaseMessageSenderService) {
        this.tokenValidation = tokenValidation;
        this.sendNotification = fireBaseMessageSenderService;
    }

    public void sendToAll(String authToken, SendNotificationRequest sendNotificationRequest) {
        try {
            tokenValidation.authTokenValidationFromUserId(authToken, sendNotificationRequest.adminId());
            sendNotification.sendToAll(sendNotificationRequest.notificationRequest());
        } catch (RedisDBException ex) {
            throw new RedisDBException(ex.getMessage(), ex);
        } catch (AuthTokenValidationException ex) {
            throw new AuthTokenValidationException(null);
        } catch (Exception ex) {
            log.error("sendToAll(): Exception occurred while sending notification to all users for request: {}, Exception: %s", toJson(sendNotificationRequest), ex.getMessage());
            throw new MongoDBReadException(ex.getMessage());
        }
    }

}
