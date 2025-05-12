package com.flick.admin.domain.user.dto.request

data class ChargeUserPointRequest(
    val userId: Long,
    val amount: Long
)