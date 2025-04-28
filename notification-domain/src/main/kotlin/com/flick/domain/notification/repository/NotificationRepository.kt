package com.flick.domain.notification.repository

import com.flick.com.flick.domain.notification.entity.Notification
import com.flick.domain.notification.enums.NotificationType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NotificationRepository : CoroutineCrudRepository<Notification, Long> {
    fun findByUserId(userId: Long): Flow<Notification>
    fun findByUserIdAndIsRead(userId: Long, isRead: Boolean): Flow<Notification>
    fun findByUserIdAndType(userId: Long, type: NotificationType): Flow<Notification>
}