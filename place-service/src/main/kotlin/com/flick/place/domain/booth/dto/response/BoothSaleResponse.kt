package com.flick.place.domain.booth.dto.response

import java.time.LocalDateTime

data class BoothSaleResponse(
    val timestamp: LocalDateTime,
    val amount: Long,
)