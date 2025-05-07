package com.flick.notification.service

import com.flick.domain.notification.enums.NotificationType

interface NotificationService {
    suspend fun createNotificationAndSend(
        userId: Long,
        type: NotificationType,
        title: String,
        body: String,
        data: String? = null
    )
}