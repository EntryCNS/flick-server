package com.flick.kiosk.repository

import com.flick.kiosk.entity.KioskSession
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface KioskSessionRepository : CoroutineCrudRepository<KioskSession, Long> {
    suspend fun findBySessionId(sessionId: String): KioskSession?
    suspend fun findByConnectionCode(connectionCode: String): KioskSession?
    fun findByBoothIdAndIsActive(boothId: Long, isActive: Boolean): Flow<KioskSession>

    @Query("SELECT * FROM kiosk_sessions WHERE is_active = true AND expires_at > NOW()")
    fun findAllActive(): Flow<KioskSession>
}