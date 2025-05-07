package com.flick.core.domain.notification.dto.response

import com.flick.domain.notification.enums.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)
