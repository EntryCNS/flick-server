package com.flick.admin.domain.auth.controller

import com.flick.admin.domain.auth.dto.request.LoginRequest
import com.flick.admin.domain.auth.dto.request.RefreshRequest
import com.flick.admin.domain.auth.service.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest) = authService.login(request)

    @PostMapping("/refresh")
    suspend fun refresh(@RequestBody request: RefreshRequest) = authService.refresh(request)
}