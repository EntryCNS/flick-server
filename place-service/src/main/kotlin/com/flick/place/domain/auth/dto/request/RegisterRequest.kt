package com.flick.place.domain.auth.dto.request

data class RegisterRequest(
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val username: String,
    val password: String,
)