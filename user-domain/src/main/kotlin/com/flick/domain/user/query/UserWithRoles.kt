package com.flick.domain.user.query

import com.flick.domain.user.enums.UserRoleType

data class UserWithRoles(
    val id: Long,
    val name: String,
    val email: String?,
    val grade: Int?,
    val room: Int?,
    val number: Int?,
    val balance: Long,
    val roles: List<UserRoleType>,
    val totalCount: Long,
    val rowNum: Long
)