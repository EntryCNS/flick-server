package com.flick.admin.domain.booth.dto.response

import com.flick.domain.booth.enums.BoothStatus
import java.time.LocalDateTime

data class BoothResponse(
    val id: Long,
    val name: String,
    val description: String,
    val status: BoothStatus,
    val totalSales: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)