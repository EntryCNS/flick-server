package com.flick.domain.notification.entity

import com.flick.domain.notification.enums.NotificationType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("notifications")
data class NotificationEntity(
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("type")
    val type: NotificationType,

    @Column("title")
    val title: String,

    @Column("body")
    val body: String,

    @Column("data")
    val data: String? = null,

    @Column("is_read")
    val isRead: Boolean = false,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)