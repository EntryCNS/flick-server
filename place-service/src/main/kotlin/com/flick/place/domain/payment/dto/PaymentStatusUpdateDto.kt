package com.flick.place.domain.payment.dto

import com.flick.domain.order.enums.PaymentMethod
import com.flick.domain.order.enums.PaymentStatus
import java.time.LocalDateTime

data class PaymentStatusUpdateDto(
    val requestId: Long,
    val orderId: Long,
    val status: PaymentStatus,
    val paymentMethod: PaymentMethod? = null,
    val amount: Long? = null,
    val processedAt: LocalDateTime = LocalDateTime.now()
)