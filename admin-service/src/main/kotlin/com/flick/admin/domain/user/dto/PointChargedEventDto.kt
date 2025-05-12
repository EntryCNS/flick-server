package com.flick.admin.domain.user.dto

data class PointChargedEventDto(
    val userId: Long,
    val amount: Long,
    val balanceAfter: Long
)
