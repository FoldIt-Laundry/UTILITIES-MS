package com.foldit.utilites.configuration;

import com.foldit.utilites.firebase.model.NotificationMessageRequest;
import com.foldit.utilites.firebase.service.FireBaseMessageSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class conut {

    @Autowired
    private FireBaseMessageSenderService fireBaseMessageSenderService;

    @PostMapping("yoy")
    public void yo(@RequestBody NotificationMessageRequest notificationMessageRequest) {
        fireBaseMessageSenderService.sendNotification(notificationMessageRequest);
    }

}
