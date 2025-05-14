package com.flick.admin.domain.statistics.dto

data class StatisticeResponse(
    val userId: Long,
    val totalCharged: Long,
    val totalUsed: Long,
    val currentBalance: Long
)