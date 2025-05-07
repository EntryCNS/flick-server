package com.flick.core.domain.payment.dto

data class OrderCompletedEvent(
    val userId: Long,
    val orderId: Long,
    val boothName: String,
    val totalAmount: Long
)