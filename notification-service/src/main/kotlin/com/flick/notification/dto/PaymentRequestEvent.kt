package com.flick.notification.dto

import java.time.LocalDateTime

data class PaymentRequestEvent(
    val userId: Long,
    val orderId: Long,
    val boothName: String,
    val totalAmount: Long,
    val token: String,
    val expiresAt: LocalDateTime
)