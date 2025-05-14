package com.flick.place.domain.auth.controller

import com.flick.place.domain.auth.dto.request.LoginRequest
import com.flick.place.domain.auth.dto.request.RefreshRequest
import com.flick.place.domain.auth.dto.request.RegisterRequest
import com.flick.place.domain.auth.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest) = authService.login(request)

    @PostMapping("/register")
    suspend fun register(@RequestBody request: RegisterRequest) {
        authService.register(request)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshRequest) = authService.refresh(request)
}