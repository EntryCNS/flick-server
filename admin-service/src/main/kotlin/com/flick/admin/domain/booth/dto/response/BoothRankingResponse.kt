package com.flick.admin.domain.booth.dto.response

data class BoothRankingResponse(
    val rank: Int,
    val id: Long,
    val name: String,
    val totalSales: Long,
)
