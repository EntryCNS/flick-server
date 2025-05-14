package com.flick.domain.notification.repository

import com.flick.domain.notification.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NotificationRepository : CoroutineCrudRepository<NotificationEntity, Long> {
    fun findAllByUserId(userId: Long): Flow<NotificationEntity>
    suspend fun findByIdAndUserId(id: Long, userId: Long): NotificationEntity?
}