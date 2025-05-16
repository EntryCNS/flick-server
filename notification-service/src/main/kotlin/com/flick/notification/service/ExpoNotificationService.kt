package com.flick.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.domain.notification.entity.NotificationEntity
import com.flick.domain.notification.enums.NotificationType
import com.flick.domain.notification.repository.NotificationRepository
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import com.flick.common.utils.logger

@Service
class ExpoNotificationService(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper,
    private val webClient: WebClient,
    private val transactionalOperator: TransactionalOperator
) : NotificationService {
    private val log = logger()

    override suspend fun createNotificationAndSend(
        userId: Long,
        type: NotificationType,
        title: String,
        body: String,
        data: String?
    ) {
        val notification = transactionalOperator.executeAndAwait {
            val notification = NotificationEntity(
                userId = userId,
                type = type,
                title = title,
                body = body,
                data = data,
                isRead = false
            )

            notificationRepository.save(notification)

            val user = userRepository.findById(userId)

            if (user == null) {
                log.error("User not found: $userId")
                return@executeAndAwait null
            }

            val pushToken = user.pushToken

            if (pushToken == null) {
                log.error("Push token not found for user: $userId")
                return@executeAndAwait null
            }

            notification to pushToken
        } ?: return

        sendPushNotification(notification.second, type, title, body, data)
    }

    private suspend fun sendPushNotification(
        token: String,
        type: NotificationType,
        title: String,
        body: String,
        data: String?
    ) {
        val dataMap = if (data != null) {
            try {
                val map = objectMapper.readValue(data, Map::class.java).toMutableMap()
                map["type"] = type.name
                map.toMap()
            } catch (e: Exception) {
                mapOf(
                    "data" to data,
                    "type" to type.name
                )
            }
        } else {
            mapOf("type" to type.name)
        }

        val requestBody = mapOf(
            "to" to token,
            "title" to title,
            "body" to body,
            "data" to dataMap,
            "sound" to "default",
            "priority" to "high",
            "channelId" to getChannelId(type)
        )

        try {
            log.info("Sending push notification: $requestBody")

            val response = webClient.post()
                .uri("https://exp.host/--/api/v2/push/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), Map::class.java)
                .retrieve()
                .bodyToMono(Map::class.java)
                .awaitSingle()

            log.info("Push notification sent successfully: $response")
        } catch (e: Exception) {
            log.error("Failed to send push notification", e)
        }
    }

    private fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.PAYMENT_REQUEST -> "payments"
            NotificationType.ORDER_COMPLETED -> "orders"
            NotificationType.POINT_CHARGED -> "points"
            NotificationType.NOTICE_CREATED -> "notices"
        }
    }
}