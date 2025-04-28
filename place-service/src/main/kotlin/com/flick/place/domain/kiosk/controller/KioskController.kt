package com.flick.place.domain.kiosk.controller

import com.flick.place.domain.kiosk.error.ConnectionCodeResponse
import com.flick.place.domain.kiosk.error.KioskSessionResponse
import com.flick.place.domain.kiosk.error.VerifyConnectionRequest
import com.flick.place.domain.kiosk.service.KioskSessionService
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/kiosks")
class KioskController(private val kioskSessionService: KioskSessionService) {
    @GetMapping("/sessions")
    suspend fun getActiveSessions(): Flow<KioskSessionResponse> =
        kioskSessionService.getActiveSessions()

    @GetMapping("/sessions/{sessionId}")
    suspend fun getSession(@PathVariable sessionId: String): KioskSessionResponse =
        kioskSessionService.getSession(sessionId)

    @PostMapping("/connect")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createConnectionCode(): ConnectionCodeResponse =
        kioskSessionService.createConnectionCode()

    @PostMapping("/verify")
    suspend fun verifyConnectionCode(
        @RequestBody request: VerifyConnectionRequest,
        @RequestParam deviceName: String
    ): KioskSessionResponse =
        kioskSessionService.verifyConnectionCode(request.connectionCode, deviceName)

    @PatchMapping("/sessions/{sessionId}/disconnect")
    suspend fun disconnectSession(@PathVariable sessionId: String): Map<String, Boolean> {
        val success = kioskSessionService.disconnectSession(sessionId)
        return mapOf("success" to success)
    }
}