package com.flick.domain.transaction.entity

import com.flick.domain.transaction.enums.TransactionType
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Table("transactions")
data class Transaction(
    @Id
    val id: Long? = null,

    @Column("user_id")
    val userId: Long,

    @Column("type")
    val type: TransactionType,

    @Column("amount")
    val amount: Long,

    @Column("balance_after")
    val balanceAfter: Long,

    @Column("order_id")
    val orderId: Long? = null,

    @CreatedDate
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)