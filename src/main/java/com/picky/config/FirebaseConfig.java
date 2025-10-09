package com.picky.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key-path}")
    private String serviceAccountKeyPath;

    private FirebaseApp firebaseApp;

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(serviceAccountKeyPath);
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                this.firebaseApp = FirebaseApp.initializeApp(options);
            } else {
                this.firebaseApp = FirebaseApp.getInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Firebase 초기화에 실패했습니다.", e);
        }
    }

    @Bean
    public FirebaseAuth getFirebaseAuth() {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
