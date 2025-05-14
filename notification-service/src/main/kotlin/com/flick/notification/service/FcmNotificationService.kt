package com.flick.notification.service

import com.flick.domain.notification.entity.NotificationEntity
import com.flick.domain.notification.enums.NotificationType
import com.flick.domain.notification.repository.NotificationRepository
import com.flick.domain.user.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.messaging.Notification as FcmNotification

//@Service
class FcmNotificationService(
    private val userRepository: UserRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val notificationRepository: NotificationRepository
) : NotificationService {
    override suspend fun createNotificationAndSend(
        userId: Long,
        type: NotificationType,
        title: String,
        body: String,
        data: String?
    ) {
        val notification = NotificationEntity(
            userId = userId,
            type = type,
            title = title,
            body = body,
            data = data,
            isRead = false
        )

        notificationRepository.save(notification)

        val user = userRepository.findById(userId) ?: return
        val fcmToken = user.pushToken ?: return

        sendPushNotification(fcmToken, title, body)
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