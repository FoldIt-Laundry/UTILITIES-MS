package com.foldit.utilites.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FireBaseConfiguration {

    @Bean
    FirebaseMessaging fireBaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(
                new ClassPathResource("foldit-ad420-firebase-adminsdk-knq0s-a2720b8e03.json")
                        .getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials).build();
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(firebaseOptions, "foldit");
        return FirebaseMessaging.getInstance(firebaseApp);
    }

}
