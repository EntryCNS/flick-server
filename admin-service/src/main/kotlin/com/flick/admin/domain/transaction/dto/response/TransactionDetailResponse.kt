package com.flick.admin.domain.transaction.dto.response

import com.flick.domain.transaction.enums.TransactionType
import java.time.LocalDateTime

data class TransactionDetailResponse(
    val id: Long,
    val user: User,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val orderId: Long? = null,
    val adminId: Long? = null,
    val memo: String? = null,
    val booth: Booth? = null,
    val items: List<OrderItem> = emptyList(),
    val createdAt: LocalDateTime
) {
    data class User(
        val id: Long,
        val name: String,
    )

    data class Booth(
        val id: Long,
        val name: String
    )

    data class Product(
        val id: Long,
        val name: String,
        val price: Long
    )

    data class OrderItem(
        val id: Long,
        val product: Product,
        val price: Long,
        val quantity: Int
    )
}