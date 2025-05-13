package com.flick.admin.domain.user.dto.response

import com.flick.domain.user.enums.UserRoleType
import java.time.LocalDateTime

data class UserInfoResponse(
    val id: Long,
    val name: String,
    val email: String? = null,
    val role: UserRoleType,
    val grade: Int? = null,
    val room: Int? = null,
    val number: Int? = null,
    val balance: Long,
    val profileUrl: String? = null,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
