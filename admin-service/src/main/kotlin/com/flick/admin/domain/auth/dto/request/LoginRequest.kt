package com.flick.admin.domain.auth.dto.request

data class LoginRequest(
    val id: String,
    val password: String
)