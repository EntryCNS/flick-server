package com.flick.admin.domain.transaction.dto.response

import com.flick.domain.transaction.enums.TransactionType
import java.time.LocalDateTime

data class TransactionResponse(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val booth: Booth? = null,
    val product: Product? = null,
    val memo: String? = null,
    val createdAt: LocalDateTime
) {
    data class Booth(val name: String)
    data class Product(val name: String)
}