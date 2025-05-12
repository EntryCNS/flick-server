package com.flick.admin.domain.user.dto.response

import com.flick.domain.user.enums.UserRoleType

data class UserResponse(
    val id: Long,
    val name: String,
    val role: UserRoleType,
    val grade: Int? = null,
    val room: Int? = null,
    val number: Int? = null,
    val balance: Long,
)
