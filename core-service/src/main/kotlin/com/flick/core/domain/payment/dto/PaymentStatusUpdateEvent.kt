package com.flick.core.domain.payment.dto

import com.flick.domain.payment.enums.PaymentMethod
import com.flick.domain.user.enums.PaymentStatus
import java.time.LocalDateTime

data class PaymentStatusUpdateEvent(
    val requestId: Long,
    val orderId: Long,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod? = null,
    val amount: Long? = null,
    val processedAt: LocalDateTime = LocalDateTime.now()
)