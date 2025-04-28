package com.flick.place.domain.booth.dto

data class RegisterBoothRequest(
    val loginId: String,
    val password: String,
    val name: String,
    val description: String? = null,
    val location: String? = null,
)