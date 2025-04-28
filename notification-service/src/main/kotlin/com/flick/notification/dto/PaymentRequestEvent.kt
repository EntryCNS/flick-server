package com.flick.notification.dto

data class PaymentRequestEvent(
    val userId: Long,
    val orderId: Long,
    val boothName: String,
    val totalAmount: Long,
    val requestCode: String
)