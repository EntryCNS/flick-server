package com.flick.domain.notification.repository

import com.flick.domain.notification.entity.NotificationEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface NotificationRepository: CoroutineCrudRepository<NotificationEntity, Long> {
}