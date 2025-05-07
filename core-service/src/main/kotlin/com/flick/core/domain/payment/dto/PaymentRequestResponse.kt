package com.flick.core.domain.payment.dto

import com.flick.domain.payment.enums.PaymentMethod

data class PaymentRequestResponse(
    val id: Long,
    val orderId: Long,
    val method: PaymentMethod,
)