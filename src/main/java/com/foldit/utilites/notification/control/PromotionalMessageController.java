package com.foldit.utilites.notification.control;

import com.foldit.utilites.exception.AuthTokenValidationException;
import com.foldit.utilites.notification.model.SendNotificationRequest;
import com.foldit.utilites.notification.service.PromotionalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.foldit.utilites.helper.JsonPrinter.toJson;

@RestController
@Slf4j
@RequestMapping("/promotion")
public class PromotionalMessageController {

    @Autowired
    private PromotionalMessageService promotionalMessageService;

    public ResponseEntity<Boolean> sendPromotionalNotificationController(@RequestBody SendNotificationRequest sendNotificationRequest, @RequestHeader(value = "authToken") String authToken) {
        try {
            log.info("sendPromotionalNotificationController(): Initiating request to send the promotional message to all user for request: {} and authToken: {}", toJson(sendNotificationRequest), authToken);
            promotionalMessageService.sendToAll(authToken, sendNotificationRequest);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (AuthTokenValidationException ex) {
            log.error("sendPromotionalNotificationController(): Auth Validation failed for userId: {} and authToken: {}, Exception: {}", sendNotificationRequest.adminId(), authToken, ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("sendPromotionalNotificationController(): Exception occurred while processing the details for payload: {}, Exception: {}", toJson(sendNotificationRequest), ex.getMessage());
            return new ResponseEntity<>(false, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

}
