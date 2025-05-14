package com.flick.domain.notification.repository

import com.flick.domain.notification.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NotificationRepository : CoroutineCrudRepository<NotificationEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): Flow<NotificationEntity>
    suspend fun findByIdAndUserId(id: Long, userId: Long): NotificationEntity?

    @Query("SELECT type, COUNT(*) as count FROM notifications GROUP BY type ORDER BY count DESC")
    suspend fun countGroupByType(): List<Map<String, Any>>
}