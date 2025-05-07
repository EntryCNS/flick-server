package com.flick.place.domain.payment.dto

data class CreateStudentIdPaymentRequest(
    val orderId: Long,
    val studentId: String,
)