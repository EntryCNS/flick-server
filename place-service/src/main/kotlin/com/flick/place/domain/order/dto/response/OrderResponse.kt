package com.flick.place.domain.order.dto.response

import com.flick.domain.order.enums.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val userId: Long?,
    val boothOrderNumber: Int,
    val totalAmount: Long,
    val status: OrderStatus,
    val expiresAt: LocalDateTime,
    val paidAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)