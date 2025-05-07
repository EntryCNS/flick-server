package com.flick.place.domain.kiosk.controller

import com.flick.place.domain.kiosk.dto.request.RegisterKioskRequest
import com.flick.place.domain.kiosk.service.KioskService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/kiosks")
class KioskController(private val kioskService: KioskService) {
    @PostMapping("/generate")
    suspend fun generateKioskRegistrationToken() = kioskService.generateKioskRegistrationToken()

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterKioskRequest) =
        kioskService.register(request)
}