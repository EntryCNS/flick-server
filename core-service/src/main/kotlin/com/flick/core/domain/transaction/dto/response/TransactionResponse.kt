package com.flick.core.domain.transaction.dto.response

import com.flick.domain.transaction.enums.TransactionType
import java.time.LocalDateTime

data class TransactionResponse(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val booth: Booth,
    val product: Product? = null,
    val memo: String?,
    val createdAt: LocalDateTime
) {
    data class Product(
        val name: String,
    )

    data class Booth(
        val name: String,
    )
}