package com.flick.admin.domain.statistics.dto

data class StatisticsResponse(
    val totalCharge: Long,
    val totalUsed: Long,
    val totalBalance: Long
)