package com.flick.kiosk.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("kiosk_sessions")
data class KioskSession(
    @Id
    val id: Long? = null,

    @Column("booth_id")
    val boothId: Long,

    @Column("session_id")
    val sessionId: String,

    @Column("device_name")
    val deviceName: String = "키오스크",

    @Column("connection_code")
    val connectionCode: String? = null,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("last_activity")
    val lastActivity: LocalDateTime = LocalDateTime.now(),

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("expires_at")
    val expiresAt: LocalDateTime = LocalDateTime.now().plusHours(24)
)