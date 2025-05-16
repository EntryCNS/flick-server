package com.flick.domain.notification.repository

import com.flick.domain.notification.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NotificationRepository : CoroutineCrudRepository<NotificationEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): Flow<NotificationEntity>
    suspend fun findByIdAndUserId(id: Long, userId: Long): NotificationEntity?

    @Query("SELECT type, COUNT(*) as count FROM notifications GROUP BY type ORDER BY count DESC")
    fun countGroupByType(): Flow<TypeCount>
}

data class TypeCount(val type: String, val count: Long)