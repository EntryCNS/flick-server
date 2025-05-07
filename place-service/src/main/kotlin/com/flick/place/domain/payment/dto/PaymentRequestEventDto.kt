package com.flick.place.domain.payment.dto

import java.time.LocalDateTime

data class PaymentRequestEventDto(
    val userId: Long,
    val orderId: Long,
    val boothName: String,
    val totalAmount: Long,
    val token: String,
    val expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(15)
)