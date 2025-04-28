package com.flick.place.domain.kiosk.service

import com.flick.common.error.CustomException
import com.flick.kiosk.entity.KioskSession
import com.flick.kiosk.error.KioskError
import com.flick.kiosk.repository.KioskSessionRepository
import com.flick.place.domain.kiosk.error.ConnectionCodeResponse
import com.flick.place.domain.kiosk.error.KioskSessionResponse
import com.flick.place.infra.security.JwtHolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Random
import java.util.UUID

@Service
class KioskSessionService(
    private val kioskSessionRepository: KioskSessionRepository,
    private val jwtHolder: JwtHolder,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {
    suspend fun getActiveSessions(): Flow<KioskSessionResponse> {
        val boothId = jwtHolder.getBoothId()
        return kioskSessionRepository.findByBoothIdAndIsActive(boothId, true)
            .map { KioskSessionResponse.from(it) }
    }

    suspend fun getSession(sessionId: String): KioskSessionResponse {
        val boothId = jwtHolder.getBoothId()
        val session = kioskSessionRepository.findBySessionId(sessionId)
            ?: throw CustomException(KioskError.SESSION_NOT_FOUND)

        if (session.boothId != boothId) {
            throw CustomException(KioskError.BOOTH_MISMATCH)
        }

        return KioskSessionResponse.from(session)
    }

    @Transactional
    suspend fun createConnectionCode(): ConnectionCodeResponse {
        val boothId = jwtHolder.getBoothId()
        val connectionCode = generateConnectionCode()

        inactivatePreviousConnectionCodes(boothId)

        val kioskSession = KioskSession(
            boothId = boothId,
            sessionId = UUID.randomUUID().toString(),
            connectionCode = connectionCode,
        )

        val saved = kioskSessionRepository.save(kioskSession)

        return ConnectionCodeResponse(
            connectionCode = connectionCode,
            expiresAt = saved.expiresAt
        )
    }

    @Transactional
    suspend fun verifyConnectionCode(connectionCode: String, deviceName: String): KioskSessionResponse {
        val session = kioskSessionRepository.findByConnectionCode(connectionCode)
            ?: throw CustomException(KioskError.CONNECTION_CODE_NOT_FOUND)

        if (!session.isActive) {
            throw CustomException(KioskError.SESSION_INACTIVE)
        }

        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            throw CustomException(KioskError.SESSION_EXPIRED)
        }

        val updatedSession = session.copy(
            deviceName = deviceName,
            connectionCode = null,
            lastActivity = LocalDateTime.now()
        )

        val savedSession = kioskSessionRepository.save(updatedSession)
        return KioskSessionResponse.from(savedSession)
    }

    @Transactional
    suspend fun disconnectSession(sessionId: String): Boolean {
        val boothId = jwtHolder.getBoothId()
        val session = kioskSessionRepository.findBySessionId(sessionId)
            ?: throw CustomException(KioskError.SESSION_NOT_FOUND)

        if (session.boothId != boothId) {
            throw CustomException(KioskError.BOOTH_MISMATCH)
        }

        val updatedSession = session.copy(
            isActive = false,
        )

        kioskSessionRepository.save(updatedSession)
        return true
    }

    suspend fun getCurrentSessionId(): Long {
        val sessionId = jwtHolder.getSessionId() ?: throw CustomException(KioskError.SESSION_NOT_FOUND)
        val session = kioskSessionRepository.findById(sessionId)
            ?: throw CustomException(KioskError.SESSION_NOT_FOUND)

        if (!session.isActive) {
            throw CustomException(KioskError.SESSION_INACTIVE)
        }

        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            throw CustomException(KioskError.SESSION_EXPIRED)
        }

        updateLastActivity(sessionId)

        return sessionId
    }

    private suspend fun updateLastActivity(sessionId: Long) {
        r2dbcEntityTemplate.update(KioskSession::class.java)
            .matching(Query.query(Criteria.where("id").`is`(sessionId)))
            .apply(Update.update("last_activity", LocalDateTime.now()))
            .subscribe()
    }

    private suspend fun inactivatePreviousConnectionCodes(boothId: Long) {
        r2dbcEntityTemplate.update(KioskSession::class.java)
            .matching(Query.query(
                Criteria.where("booth_id").`is`(boothId)
                    .and("connection_code").isNotNull()
                    .and("is_active").`is`(true)
            ))
            .apply(Update.update("is_active", false))
            .subscribe()
    }

    private fun generateConnectionCode(): String {
        val characters = "0123456789"
        val random = Random()
        val codeBuilder = StringBuilder()

        repeat(6) {
            codeBuilder.append(characters[random.nextInt(characters.length)])
        }

        return codeBuilder.toString()
    }
}