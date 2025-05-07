package com.flick.place.domain.payment.dto

data class CreateQrPaymentResponse(
    val id: Long,
    val token: String,
)