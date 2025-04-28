package com.flick.notification.service

import com.flick.com.flick.domain.notification.entity.Notification
import com.flick.domain.notification.enums.NotificationType
import com.flick.domain.notification.repository.NotificationRepository
import com.flick.domain.user.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification as FcmNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun createNotificationAndSend(
        userId: Long,
        type: NotificationType,
        title: String,
        content: String,
        data: String? = null
    ) {
        val notification = Notification(
            userId = userId,
            type = type,
            title = title,
            content = content,
            data = data,
            isRead = false
        )

        notificationRepository.save(notification)

        val user = userRepository.findById(userId) ?: return
        val deviceToken = user.deviceToken ?: return

        sendPushNotification(deviceToken, title, content)
    }

    private suspend fun sendPushNotification(token: String, title: String, body: String) {
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                try {
                    val message = Message.builder()
                        .setToken(token)
                        .setNotification(
                            FcmNotification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                        )
                        .build()

                    val response = firebaseMessaging.send(message)
                    continuation.resume(response)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }
}