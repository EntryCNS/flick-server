package com.flick.admin.domain.booth.dto.response

import java.time.LocalDateTime

data class BoothRankingResponse(
    val id: Long,
    val totalSales: Long,
    val name: String,
    val timestamp: LocalDateTime,
)
