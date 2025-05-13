package com.flick.admin.domain.transaction.dto.response

import com.flick.domain.transaction.enums.TransactionType
import java.time.LocalDateTime

data class TransactionResponse(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val balanceAfter: Long,
    val createdAt: LocalDateTime
)