package com.flick.core.domain.auth.dto.request

data class LoginRequest(
    val id: String,
    val password: String
)