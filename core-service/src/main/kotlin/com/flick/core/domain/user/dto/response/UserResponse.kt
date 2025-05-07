package com.flick.core.domain.user.dto.response

import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val dAuthId: String,
    val name: String,
    val email: String?,
    val grade: Int?,
    val room: Int?,
    val number: Int?,
    val profileUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)