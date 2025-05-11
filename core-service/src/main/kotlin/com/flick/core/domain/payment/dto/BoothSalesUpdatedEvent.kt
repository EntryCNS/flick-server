package com.flick.core.domain.payment.dto

data class BoothSalesUpdatedEvent(
    val boothId: Long,
    val totalSales: Long,
)
