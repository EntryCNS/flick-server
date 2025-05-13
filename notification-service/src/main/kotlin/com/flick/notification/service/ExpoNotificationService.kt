package com.flick.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.flick.domain.notification.entity.NotificationEntity
import com.flick.domain.notification.enums.NotificationType
import com.flick.domain.notification.repository.NotificationRepository
import com.flick.domain.user.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ExpoNotificationService(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val objectMapper: ObjectMapper,
    private val webClient: WebClient,
    private val transactionalOperator: TransactionalOperator
) : NotificationService {
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

            val user = userRepository.findById(userId) ?: return@executeAndAwait null
            val pushToken = user.pushToken ?: return@executeAndAwait null

            notification to pushToken
        } ?: return

        sendExpoPushNotification(notification.second, type, title, body, data)
    }

    private suspend fun sendExpoPushNotification(
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
            "priority" to "high"
        )

        withContext(Dispatchers.IO) {
            webClient.post()
                .uri("https://exp.host/--/api/v2/push/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), Map::class.java)
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
        }
    }
}