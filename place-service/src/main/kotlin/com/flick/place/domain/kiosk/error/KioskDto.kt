package com.flick.place.domain.kiosk.error

import com.flick.kiosk.entity.KioskSession
import java.time.LocalDateTime

data class ConnectKioskRequest(
    val deviceName: String
)

data class VerifyConnectionRequest(
    val connectionCode: String
)

data class KioskSessionResponse(
    val id: Long,
    val sessionId: String,
    val deviceName: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime
) {
    companion object {
        fun from(kioskSession: KioskSession) =
            KioskSessionResponse(
                id = kioskSession.id!!,
                sessionId = kioskSession.sessionId,
                deviceName = kioskSession.deviceName,
                isActive = kioskSession.isActive,
                createdAt = kioskSession.createdAt,
                expiresAt = kioskSession.expiresAt
            )
    }
}

data class ConnectionCodeResponse(
    val connectionCode: String,
    val expiresAt: LocalDateTime
)