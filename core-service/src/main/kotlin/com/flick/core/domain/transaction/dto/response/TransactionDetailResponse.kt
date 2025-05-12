package com.flick.core.domain.transaction.dto.response

import com.flick.domain.transaction.enums.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

data class TransactionDetailResponse(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val memo: String?,
    val createdAt: LocalDateTime,
    val booth: Booth,
    val items: List<OrderItem>,
) {
    data class Booth(
        val id: Long,
        val name: String
    )

    data class OrderItem(
        val id: Long,
        val product: Product,
        val quantity: Int,
        val price: Long,
    )

    data class Product(
        val id: Long,
        val name: String,
        val price: Long,
    )
}