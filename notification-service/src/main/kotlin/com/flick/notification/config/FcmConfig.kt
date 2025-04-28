package com.flick.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class FcmConfig {
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val resource = ClassPathResource("firebase-service-account.json")
        val credentials = GoogleCredentials.fromStream(resource.inputStream)

        val options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            FirebaseMessaging.getInstance()
        } else {
            FirebaseMessaging.getInstance()
        }
    }
}