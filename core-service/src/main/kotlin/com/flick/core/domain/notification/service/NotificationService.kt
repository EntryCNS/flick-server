package com.flick.core.domain.notification.service

import com.flick.common.error.CustomException
import com.flick.core.domain.notification.dto.response.NotificationResponse
import com.flick.core.infra.security.SecurityHolder
import com.flick.domain.notification.error.NotificationError
import com.flick.domain.notification.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val securityHolder: SecurityHolder
) {
    suspend fun getMyNotifications(): Flow<NotificationResponse> {
        val userId = securityHolder.getUserId()
        val notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)

        return notifications.map { notification ->
            NotificationResponse(
                id = notification.id!!,
                title = notification.title,
                body = notification.body,
                type = notification.type,
                isRead = notification.isRead,
                createdAt = notification.createdAt
            )
        }
    }

    suspend fun readNotification(notificationId: Long) {
        val userId = securityHolder.getUserId()
        val notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            ?: throw CustomException(NotificationError.NOTIFICATION_NOT_FOUND)

        notificationRepository.save(
            notification.copy(
                isRead = true
            )
        )
    }
}