package com.flick.notification.dto

data class PointChargedEvent(
    val userId: Long,
    val amount: Long,
    val balanceAfter: Long
)